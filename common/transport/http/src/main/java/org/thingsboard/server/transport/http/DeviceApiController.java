/**
 * Copyright © 2016-2021 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.transport.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.DeviceTransportType;
import org.thingsboard.server.common.data.TbTransportService;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.ota.OtaPackageType;
import org.thingsboard.server.common.data.rpc.RpcStatus;
import org.thingsboard.server.common.transport.SessionMsgListener;
import org.thingsboard.server.common.transport.TransportContext;
import org.thingsboard.server.common.transport.TransportService;
import org.thingsboard.server.common.transport.TransportServiceCallback;
import org.thingsboard.server.common.transport.adaptor.JsonConverter;
import org.thingsboard.server.common.transport.auth.SessionInfoCreator;
import org.thingsboard.server.common.transport.auth.ValidateDeviceCredentialsResponse;
import org.thingsboard.server.dao.model.sql.Cammercap;
import org.thingsboard.server.dao.model.sql.Cammercus;
import org.thingsboard.server.dao.model.sql.Cammernum;
import org.thingsboard.server.dao.sql.cammercap.CammercapRepository;
import org.thingsboard.server.dao.sql.cammercus.CammercusRepository;
import org.thingsboard.server.dao.sql.cammernum.CammernumRepository;
import org.thingsboard.server.gen.transport.TransportProtos;
import org.thingsboard.server.gen.transport.TransportProtos.AttributeUpdateNotificationMsg;
import org.thingsboard.server.gen.transport.TransportProtos.GetAttributeRequestMsg;
import org.thingsboard.server.gen.transport.TransportProtos.GetAttributeResponseMsg;
import org.thingsboard.server.gen.transport.TransportProtos.ProvisionDeviceResponseMsg;
import org.thingsboard.server.gen.transport.TransportProtos.SessionCloseNotificationProto;
import org.thingsboard.server.gen.transport.TransportProtos.SessionInfoProto;
import org.thingsboard.server.gen.transport.TransportProtos.SubscribeToAttributeUpdatesMsg;
import org.thingsboard.server.gen.transport.TransportProtos.SubscribeToRPCMsg;
import org.thingsboard.server.gen.transport.TransportProtos.ToDeviceRpcRequestMsg;
import org.thingsboard.server.gen.transport.TransportProtos.ToDeviceRpcResponseMsg;
import org.thingsboard.server.gen.transport.TransportProtos.ToServerRpcRequestMsg;
import org.thingsboard.server.gen.transport.TransportProtos.ToServerRpcResponseMsg;
import org.thingsboard.server.gen.transport.TransportProtos.ValidateDeviceTokenRequestMsg;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Andrew Shvayka
 */
@RestController
@ConditionalOnExpression("'${service.type:null}'=='tb-transport' || ('${service.type:null}'=='monolith' && '${transport.api_enabled:true}'=='true' && '${transport.http.enabled}'=='true')")
@RequestMapping("/api/v1")
@Slf4j
public class DeviceApiController implements TbTransportService {

    @Autowired
    private HttpTransportContext transportContext;
    String changeJson = "";//下面遥测接口存储不同报文信息

    @Autowired
    private CammercusRepository cammercusRepository;

    @Autowired
    private CammercapRepository cammercapRepository;

    @Autowired
    private CammernumRepository cammernumRepository;

    @RequestMapping(value = "/{deviceToken}/attributes", method = RequestMethod.GET, produces = "application/json")
    public DeferredResult<ResponseEntity> getDeviceAttributes(@PathVariable("deviceToken") String deviceToken,
                                                              @RequestParam(value = "clientKeys", required = false, defaultValue = "") String clientKeys,
                                                              @RequestParam(value = "sharedKeys", required = false, defaultValue = "") String sharedKeys,
                                                              HttpServletRequest httpRequest) {
        DeferredResult<ResponseEntity> responseWriter = new DeferredResult<>();
        transportContext.getTransportService().process(DeviceTransportType.DEFAULT, ValidateDeviceTokenRequestMsg.newBuilder().setToken(deviceToken).build(),
                new DeviceAuthCallback(transportContext, responseWriter, sessionInfo -> {
                    GetAttributeRequestMsg.Builder request = GetAttributeRequestMsg.newBuilder().setRequestId(0);
                    List<String> clientKeySet = !StringUtils.isEmpty(clientKeys) ? Arrays.asList(clientKeys.split(",")) : null;
                    List<String> sharedKeySet = !StringUtils.isEmpty(sharedKeys) ? Arrays.asList(sharedKeys.split(",")) : null;
                    if (clientKeySet != null) {
                        request.addAllClientAttributeNames(clientKeySet);
                    }
                    if (sharedKeySet != null) {
                        request.addAllSharedAttributeNames(sharedKeySet);
                    }
                    TransportService transportService = transportContext.getTransportService();
                    transportService.registerSyncSession(sessionInfo,
                            new HttpSessionListener(responseWriter, transportContext.getTransportService(), sessionInfo),
                            transportContext.getDefaultTimeout());
                    transportService.process(sessionInfo, request.build(), new SessionCloseOnErrorCallback(transportService, sessionInfo));
                }));
        return responseWriter;
    }

    @RequestMapping(value = "/{deviceToken}/attributes", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity> postDeviceAttributes(@PathVariable("deviceToken") String deviceToken,
                                                               @RequestBody String json, HttpServletRequest request) {
        DeferredResult<ResponseEntity> responseWriter = new DeferredResult<>();
        transportContext.getTransportService().process(DeviceTransportType.DEFAULT, ValidateDeviceTokenRequestMsg.newBuilder().setToken(deviceToken).build(),
                new DeviceAuthCallback(transportContext, responseWriter, sessionInfo -> {
                    TransportService transportService = transportContext.getTransportService();
                    transportService.process(sessionInfo, JsonConverter.convertToAttributesProto(new JsonParser().parse(json)),
                            new HttpOkCallback(responseWriter));
                }));
        return responseWriter;
    }

    @RequestMapping(value = "/{deviceToken}/telemetry", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity> postTelemetry(@PathVariable("deviceToken") String deviceToken,
                                                        @RequestBody(required = false) String json,
                                                        HttpServletRequest request) throws ParseException {
        DeferredResult<ResponseEntity> responseWriter = new DeferredResult<ResponseEntity>();
        if(!"".equals(json)&&null!=json) {
            //System.out.println("***客流统计开始***");
            //System.out.println("result客流: " + json);
            Document doc = null;
            try {
                doc = DocumentHelper.parseText(json);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            Element root = doc.getRootElement();
            String ipAddress = root.element("ipAddress").getText();
            String macAddress = root.element("macAddress").getText();
            String eventType ="1";
            String direction ="0";
            String eventTime  = root.element("dateTime").getText();
            String curtime = eventTime.substring(0,10);
            String secTime = eventTime.substring(11,19);
            eventTime  = curtime+" "+ secTime;
            System.out.println("客流时间： ***"+ eventTime);
            //把日期字符串转成是时间戳
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String finalStr = String.valueOf(sdf.parse(eventTime).getTime());//字符串形式的事件时间戳
            long logTime=0;
            Date date = sdf.parse(eventTime);
            logTime = date.getTime();//事件发生时间戳
            Long curTime = System.currentTimeMillis();//当前系统时间戳
            //转化时间结束

            Element childEle = root.element("peopleCounting");

            Element macele = childEle.element("enter");
            String enter = macele.getTextTrim();
            if("".equals(enter)||null==enter){enter = "0";}
            int intenter = Integer.valueOf(enter);

            Element eveTyp = childEle.element("exit");
            String exit = eveTyp.getTextTrim();
            if("".equals(exit)||null==exit){exit = "0";}
            int inexit = Integer.valueOf(exit);

            Element ipobj = childEle.element("duplicatePeople");
            String duplic = ipobj.getTextTrim();

            String gender = "0";
            String age = "0";
            String mask = "0";
            String hat = "0";
            String beard = "0";
            String glass = "0";
            //把客流数据加入到自己创建的表中，供后期使用
            //在这里里面就要把方向确定，其他时机不合适，离开和进入的数据存在配置文件还是数据库中
            //Cammernum numObj= cammernumRepository.findByIpAddressAndMacAddress(ipAddress,macAddress);
            Cammernum numObj= cammernumRepository.findByMacAddress(macAddress);
            if(null==numObj || "".equals(numObj)){//摄像头第一次上报数据；情况可能是：第一次为进，或者第一次为出,保存到数据库中
                //第一次上报过，进出只做更新
                String rid=UUID.randomUUID().toString().replaceAll("-", "");
                cammernumRepository.save(new Cammernum(rid,intenter,inexit,curTime,ipAddress,macAddress));
                //判断方向
                if(intenter>0){//进入
                    direction="0";
                }else if(inexit>0){//出去
                    direction="1";
                }
            }else{//假如不是第一次，已经有了数据，第一次上报过，进出只做更新
                //如果已经存在数据了，当清零完，那么再次进入时，要么进入数为0，出去数为1；要么进入数为1，出去数为0
                if((intenter>0&&inexit==0)||(intenter==0&&inexit>0)){
                    //cammernumRepository.modifyByIpAddressAndMacAddress(intenter,inexit,ipAddress,macAddress);
                    cammernumRepository.modifyByMacAddress(intenter,inexit,macAddress);
                    if(intenter>0&&inexit==0){//进入
                        direction="0";
                    }else{//出去
                        direction="1";
                    }
                }else{//未清零的情况下,一天中正常进出时的统计
                    int getEnter = numObj.getEnter();
                    int getExit =  numObj.getExit();
                    //判断进出,假如进入数大于数据库中的数则是表示进入;如果离开数大于数据库中的数表示离开
                    if(intenter > getEnter){
                        direction="0";
                    }

                    if(inexit > getExit){
                        direction="1";
                    }
                    //cammernumRepository.modifyByIpAddressAndMacAddress(intenter,inexit,ipAddress,macAddress);
                    cammernumRepository.modifyByMacAddress(intenter,inexit,macAddress);
                }
            }

            //方向处理结束

            //String strUid = UUID.randomUUID().toString().replaceAll("-", "");
            //cammercusRepository.save(new Cammercus(strUid,curTime,ipAddress,macAddress,direction,logTime,enter,exit,duplic,"0","1"));
            //创建一个json字符串,根据传入的数据
            //String changeJson="{\"ipAddress\":\"192.168.100.102\",\"eventType\":\"linedetection\",\"macAddress\":\"kdkkk3kk3\"}";
            changeJson = "{\"ipAddress\":\""+ipAddress+"\",\"macAddress\":\""+macAddress+"\",\"eventType\":\""+eventType+"\",\"direction\":\""+direction+"\"," +
                    "\"eventTime\":\""+logTime+"\",\"enter\":\"" + enter + "\",\"exit\":\"" + exit + "\",\"duplicatePeople\":\"" + duplic + "\"," +
                    "\"gender\":\""+gender+"\",\"age\":\""+age+"\",\"mask\":\""+mask+"\",\"glass\":\""+glass+"\",\"hat\":\""+hat+"\",\"beard\":\""+beard+"\"}";
        }else{
            //System.out.println("***抓图开始***");
            MultipartHttpServletRequest murRes=((MultipartHttpServletRequest) request);
            String faceJson=murRes.getParameter("faceCapture");
            JsonNode gjnodes = JacksonUtil.toJsonNode(faceJson);
            String pone = gjnodes.findPath("faceCapture").findPath("targetAttrs").findPath("pId").asText();
            String ptwo = gjnodes.findPath("faceCapture").findPath("faces").findPath("pId").asText();
            MultipartFile files = murRes.getFile(ptwo);
            try {
                byte[] byteary = files.getBytes();
                // System.out.println("***流***："+files.getInputStream()+"****"+files.getSize());
            } catch (IOException e) {
                e.printStackTrace();
            }

            JsonNode jnode = JacksonUtil.toJsonNode(faceJson);
            //String ip = jnode.get("ipAddress").asText();
            //System.out.println("********我来了*********************");
            String ipAddress = jnode.get("ipAddress").asText();
            String macAddress = jnode.get("macAddress").asText();
            String eventType ="0";
            String direction = "0";
            String eventTime  = jnode.get("dateTime").asText();
            String curtime = eventTime.substring(0,10);
            String secTime = eventTime.substring(11,19);
            eventTime  = curtime+" "+ secTime;
            System.out.println("***抓图开始***");
            System.out.println("抓图时间： ***"+ eventTime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String finalStr = String.valueOf(sdf.parse(eventTime).getTime());
            long enTim=0;
            Date date = sdf.parse(eventTime);
            enTim = date.getTime();//事件发生时间戳
            //获取时间结束
            String enter="0";
            String exit ="0";
            String duplic ="0";

            String gender = jnode.findPath("faceCapture").findPath("faces").findPath("gender").get("value").asText();
            if("male".equals(gender)){gender = "0";}else{gender = "1";};
            String age = jnode.findPath("faceCapture").findPath("faces").findPath("age").get("value").asText();
            int finalAge = Integer.valueOf(age);
            String mask = jnode.findPath("faceCapture").findPath("faces").findPath("mask").get("value").asText();
            if("no".equals(mask)){mask = "0";}else{mask = "1";};
            String glass = jnode.findPath("faceCapture").findPath("faces").findPath("glass").get("value").asText();
            if("no".equals(glass)){glass = "0";}else{glass = "1";};
            String hat = jnode.findPath("faceCapture").findPath("faces").findPath("hat").get("value").asText();
            if("no".equals(hat)){hat = "0";}else{hat = "1";};
            String beard = jnode.findPath("faceCapture").findPath("faces").findPath("beard").get("value").asText();
            if("no".equals(beard)){beard = "0";}else{beard = "1";};
            //String enterTime = jnode.findPath("faceCapture").findPath("faces").findPath("enterTime").asText();
           // String exitTime = jnode.findPath("faceCapture").findPath("faces").findPath("exitTime").asText();
            //String gpIds = jnode.findPath("faceCapture").findPath("faces").findPath("pId").asText();

            //测试方案使用，包数据保存到自己创建的表中去
            Long curTime = System.currentTimeMillis();
            //String str = UUID.randomUUID().toString().replaceAll("-", "");
            //cammercapRepository.save(new Cammercap(str,gender,finalAge,mask,glass,hat,beard,ipAddress,macAddress,enTim,curTime,"0","0"));
            changeJson = "{\"ipAddress\":\""+ipAddress+"\",\"macAddress\":\""+macAddress+"\",\"eventType\":\""+eventType+"\",\"direction\":\""+direction+"\"," +
                    "\"eventTime\":\""+enTim+"\",\"enter\":\"" + enter + "\",\"exit\":\"" + exit + "\",\"duplicatePeople\":\"" + duplic + "\"," +
                    "\"gender\":\""+gender+"\",\"age\":\""+age+"\",\"mask\":\""+mask+"\",\"glass\":\""+glass+"\",\"hat\":\""+hat+"\",\"beard\":\""+beard+"\"}";
            //System.out.print("********************************输出*****："+changeJson);
          }
        System.out.print("********************************输出*****："+changeJson);
        transportContext.getTransportService().process(DeviceTransportType.DEFAULT, ValidateDeviceTokenRequestMsg.newBuilder().setToken(deviceToken).build(),
                new DeviceAuthCallback(transportContext, responseWriter, sessionInfo -> {
                    TransportService transportService = transportContext.getTransportService();
                    transportService.process(sessionInfo, JsonConverter.convertToTelemetryProto(new JsonParser().parse(changeJson)),
                            new HttpOkCallback(responseWriter));
                }));
        return responseWriter;
    }

    @RequestMapping(value = "/{deviceToken}/claim", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity> claimDevice(@PathVariable("deviceToken") String deviceToken,
                                                      @RequestBody(required = false) String json, HttpServletRequest request) {
        DeferredResult<ResponseEntity> responseWriter = new DeferredResult<>();
        transportContext.getTransportService().process(DeviceTransportType.DEFAULT, ValidateDeviceTokenRequestMsg.newBuilder().setToken(deviceToken).build(),
                new DeviceAuthCallback(transportContext, responseWriter, sessionInfo -> {
                    TransportService transportService = transportContext.getTransportService();
                    DeviceId deviceId = new DeviceId(new UUID(sessionInfo.getDeviceIdMSB(), sessionInfo.getDeviceIdLSB()));
                    transportService.process(sessionInfo, JsonConverter.convertToClaimDeviceProto(deviceId, json),
                            new HttpOkCallback(responseWriter));
                }));
        return responseWriter;
    }

    @RequestMapping(value = "/{deviceToken}/rpc", method = RequestMethod.GET, produces = "application/json")
    public DeferredResult<ResponseEntity> subscribeToCommands(@PathVariable("deviceToken") String deviceToken,
                                                              @RequestParam(value = "timeout", required = false, defaultValue = "0") long timeout,
                                                              HttpServletRequest httpRequest) {
        DeferredResult<ResponseEntity> responseWriter = new DeferredResult<>();
        transportContext.getTransportService().process(DeviceTransportType.DEFAULT, ValidateDeviceTokenRequestMsg.newBuilder().setToken(deviceToken).build(),
                new DeviceAuthCallback(transportContext, responseWriter, sessionInfo -> {
                    TransportService transportService = transportContext.getTransportService();
                    transportService.registerSyncSession(sessionInfo,
                            new HttpSessionListener(responseWriter, transportContext.getTransportService(), sessionInfo),
                            timeout == 0 ? transportContext.getDefaultTimeout() : timeout);
                    transportService.process(sessionInfo, SubscribeToRPCMsg.getDefaultInstance(),
                            new SessionCloseOnErrorCallback(transportService, sessionInfo));

                }));
        return responseWriter;
    }

    @RequestMapping(value = "/{deviceToken}/rpc/{requestId}", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity> replyToCommand(@PathVariable("deviceToken") String deviceToken,
                                                         @PathVariable("requestId") Integer requestId,
                                                         @RequestBody String json, HttpServletRequest request) {
        DeferredResult<ResponseEntity> responseWriter = new DeferredResult<ResponseEntity>();
        transportContext.getTransportService().process(DeviceTransportType.DEFAULT, ValidateDeviceTokenRequestMsg.newBuilder().setToken(deviceToken).build(),
                new DeviceAuthCallback(transportContext, responseWriter, sessionInfo -> {
                    TransportService transportService = transportContext.getTransportService();
                    transportService.process(sessionInfo, ToDeviceRpcResponseMsg.newBuilder().setRequestId(requestId).setPayload(json).build(), new HttpOkCallback(responseWriter));
                }));
        return responseWriter;
    }

    @RequestMapping(value = "/{deviceToken}/rpc", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity> postRpcRequest(@PathVariable("deviceToken") String deviceToken,
                                                         @RequestBody String json, HttpServletRequest httpRequest) {
        DeferredResult<ResponseEntity> responseWriter = new DeferredResult<ResponseEntity>();
        transportContext.getTransportService().process(DeviceTransportType.DEFAULT, ValidateDeviceTokenRequestMsg.newBuilder().setToken(deviceToken).build(),
                new DeviceAuthCallback(transportContext, responseWriter, sessionInfo -> {
                    JsonObject request = new JsonParser().parse(json).getAsJsonObject();
                    TransportService transportService = transportContext.getTransportService();
                    transportService.registerSyncSession(sessionInfo,
                            new HttpSessionListener(responseWriter, transportContext.getTransportService(), sessionInfo),
                            transportContext.getDefaultTimeout());
                    transportService.process(sessionInfo, ToServerRpcRequestMsg.newBuilder().setRequestId(0)
                                    .setMethodName(request.get("method").getAsString())
                                    .setParams(request.get("params").toString()).build(),
                            new SessionCloseOnErrorCallback(transportService, sessionInfo));
                }));
        return responseWriter;
    }

    @RequestMapping(value = "/{deviceToken}/attributes/updates", method = RequestMethod.GET, produces = "application/json")
    public DeferredResult<ResponseEntity> subscribeToAttributes(@PathVariable("deviceToken") String deviceToken,
                                                                @RequestParam(value = "timeout", required = false, defaultValue = "0") long timeout,
                                                                HttpServletRequest httpRequest) {
        DeferredResult<ResponseEntity> responseWriter = new DeferredResult<>();
        transportContext.getTransportService().process(DeviceTransportType.DEFAULT, ValidateDeviceTokenRequestMsg.newBuilder().setToken(deviceToken).build(),
                new DeviceAuthCallback(transportContext, responseWriter, sessionInfo -> {
                    TransportService transportService = transportContext.getTransportService();
                    transportService.registerSyncSession(sessionInfo,
                            new HttpSessionListener(responseWriter, transportContext.getTransportService(), sessionInfo),
                            timeout == 0 ? transportContext.getDefaultTimeout() : timeout);
                    transportService.process(sessionInfo, SubscribeToAttributeUpdatesMsg.getDefaultInstance(),
                            new SessionCloseOnErrorCallback(transportService, sessionInfo));

                }));
        return responseWriter;
    }

    @RequestMapping(value = "/{deviceToken}/firmware", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity> getFirmware(@PathVariable("deviceToken") String deviceToken,
                                                      @RequestParam(value = "title") String title,
                                                      @RequestParam(value = "version") String version,
                                                      @RequestParam(value = "size", required = false, defaultValue = "0") int size,
                                                      @RequestParam(value = "chunk", required = false, defaultValue = "0") int chunk) {
        return getOtaPackageCallback(deviceToken, title, version, size, chunk, OtaPackageType.FIRMWARE);
    }

    @RequestMapping(value = "/{deviceToken}/software", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity> getSoftware(@PathVariable("deviceToken") String deviceToken,
                                                      @RequestParam(value = "title") String title,
                                                      @RequestParam(value = "version") String version,
                                                      @RequestParam(value = "size", required = false, defaultValue = "0") int size,
                                                      @RequestParam(value = "chunk", required = false, defaultValue = "0") int chunk) {
        return getOtaPackageCallback(deviceToken, title, version, size, chunk, OtaPackageType.SOFTWARE);
    }

    @RequestMapping(value = "/provision", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity> provisionDevice(@RequestBody String json, HttpServletRequest httpRequest) {
        DeferredResult<ResponseEntity> responseWriter = new DeferredResult<>();
        transportContext.getTransportService().process(JsonConverter.convertToProvisionRequestMsg(json),
                new DeviceProvisionCallback(responseWriter));
        return responseWriter;
    }

    private DeferredResult<ResponseEntity> getOtaPackageCallback(String deviceToken, String title, String version, int size, int chunk, OtaPackageType firmwareType) {
        DeferredResult<ResponseEntity> responseWriter = new DeferredResult<>();
        transportContext.getTransportService().process(DeviceTransportType.DEFAULT, ValidateDeviceTokenRequestMsg.newBuilder().setToken(deviceToken).build(),
                new DeviceAuthCallback(transportContext, responseWriter, sessionInfo -> {
                    TransportProtos.GetOtaPackageRequestMsg requestMsg = TransportProtos.GetOtaPackageRequestMsg.newBuilder()
                            .setTenantIdMSB(sessionInfo.getTenantIdMSB())
                            .setTenantIdLSB(sessionInfo.getTenantIdLSB())
                            .setDeviceIdMSB(sessionInfo.getDeviceIdMSB())
                            .setDeviceIdLSB(sessionInfo.getDeviceIdLSB())
                            .setType(firmwareType.name()).build();
                    transportContext.getTransportService().process(sessionInfo, requestMsg, new GetOtaPackageCallback(responseWriter, title, version, size, chunk));
                }));
        return responseWriter;
    }

    private static class DeviceAuthCallback implements TransportServiceCallback<ValidateDeviceCredentialsResponse> {
        private final TransportContext transportContext;
        private final DeferredResult<ResponseEntity> responseWriter;
        private final Consumer<SessionInfoProto> onSuccess;

        DeviceAuthCallback(TransportContext transportContext, DeferredResult<ResponseEntity> responseWriter, Consumer<SessionInfoProto> onSuccess) {
            this.transportContext = transportContext;
            this.responseWriter = responseWriter;
            this.onSuccess = onSuccess;
        }

        @Override
        public void onSuccess(ValidateDeviceCredentialsResponse msg) {
            if (msg.hasDeviceInfo()) {
                onSuccess.accept(SessionInfoCreator.create(msg, transportContext, UUID.randomUUID()));
            } else {
                responseWriter.setResult(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
            }
        }

        @Override
        public void onError(Throwable e) {
            log.warn("Failed to process request", e);
            responseWriter.setResult(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    private static class DeviceProvisionCallback implements TransportServiceCallback<ProvisionDeviceResponseMsg> {
        private final DeferredResult<ResponseEntity> responseWriter;

        DeviceProvisionCallback(DeferredResult<ResponseEntity> responseWriter) {
            this.responseWriter = responseWriter;
        }

        @Override
        public void onSuccess(ProvisionDeviceResponseMsg msg) {
            responseWriter.setResult(new ResponseEntity<>(JsonConverter.toJson(msg).toString(), HttpStatus.OK));
        }

        @Override
        public void onError(Throwable e) {
            log.warn("Failed to process request", e);
            responseWriter.setResult(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    private class GetOtaPackageCallback implements TransportServiceCallback<TransportProtos.GetOtaPackageResponseMsg> {
        private final DeferredResult<ResponseEntity> responseWriter;
        private final String title;
        private final String version;
        private final int chuckSize;
        private final int chuck;

        GetOtaPackageCallback(DeferredResult<ResponseEntity> responseWriter, String title, String version, int chuckSize, int chuck) {
            this.responseWriter = responseWriter;
            this.title = title;
            this.version = version;
            this.chuckSize = chuckSize;
            this.chuck = chuck;
        }

        @Override
        public void onSuccess(TransportProtos.GetOtaPackageResponseMsg otaPackageResponseMsg) {
            if (!TransportProtos.ResponseStatus.SUCCESS.equals(otaPackageResponseMsg.getResponseStatus())) {
                responseWriter.setResult(new ResponseEntity<>(HttpStatus.NOT_FOUND));
            } else if (title.equals(otaPackageResponseMsg.getTitle()) && version.equals(otaPackageResponseMsg.getVersion())) {
                String otaPackageId = new UUID(otaPackageResponseMsg.getOtaPackageIdMSB(), otaPackageResponseMsg.getOtaPackageIdLSB()).toString();
                ByteArrayResource resource = new ByteArrayResource(transportContext.getOtaPackageDataCache().get(otaPackageId, chuckSize, chuck));
                ResponseEntity<ByteArrayResource> response = ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + otaPackageResponseMsg.getFileName())
                        .header("x-filename", otaPackageResponseMsg.getFileName())
                        .contentLength(resource.contentLength())
                        .contentType(parseMediaType(otaPackageResponseMsg.getContentType()))
                        .body(resource);
                responseWriter.setResult(response);
            } else {
                responseWriter.setResult(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
            }
        }

        @Override
        public void onError(Throwable e) {
            log.warn("Failed to process request", e);
            responseWriter.setResult(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    private static class SessionCloseOnErrorCallback implements TransportServiceCallback<Void> {
        private final TransportService transportService;
        private final SessionInfoProto sessionInfo;

        SessionCloseOnErrorCallback(TransportService transportService, SessionInfoProto sessionInfo) {
            this.transportService = transportService;
            this.sessionInfo = sessionInfo;
        }

        @Override
        public void onSuccess(Void msg) {
        }

        @Override
        public void onError(Throwable e) {
            transportService.deregisterSession(sessionInfo);
        }
    }

    private static class HttpOkCallback implements TransportServiceCallback<Void> {
        private final DeferredResult<ResponseEntity> responseWriter;

        public HttpOkCallback(DeferredResult<ResponseEntity> responseWriter) {
            this.responseWriter = responseWriter;
        }

        @Override
        public void onSuccess(Void msg) {
            responseWriter.setResult(new ResponseEntity<>(HttpStatus.OK));
        }

        @Override
        public void onError(Throwable e) {
            responseWriter.setResult(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @RequiredArgsConstructor
    private static class HttpSessionListener implements SessionMsgListener {

        private final DeferredResult<ResponseEntity> responseWriter;
        private final TransportService transportService;
        private final SessionInfoProto sessionInfo;

        @Override
        public void onGetAttributesResponse(GetAttributeResponseMsg msg) {
            responseWriter.setResult(new ResponseEntity<>(JsonConverter.toJson(msg).toString(), HttpStatus.OK));
        }

        @Override
        public void onAttributeUpdate(UUID sessionId, AttributeUpdateNotificationMsg msg) {
            log.trace("[{}] Received attributes update notification to device", sessionId);
            responseWriter.setResult(new ResponseEntity<>(JsonConverter.toJson(msg).toString(), HttpStatus.OK));
        }

        @Override
        public void onRemoteSessionCloseCommand(UUID sessionId, SessionCloseNotificationProto sessionCloseNotification) {
            log.trace("[{}] Received the remote command to close the session: {}", sessionId, sessionCloseNotification.getMessage());
            responseWriter.setResult(new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT));
        }

        @Override
        public void onToDeviceRpcRequest(UUID sessionId, ToDeviceRpcRequestMsg msg) {
            log.trace("[{}] Received RPC command to device", sessionId);
            responseWriter.setResult(new ResponseEntity<>(JsonConverter.toJson(msg, true).toString(), HttpStatus.OK));
            transportService.process(sessionInfo, msg, RpcStatus.DELIVERED, TransportServiceCallback.EMPTY);
        }

        @Override
        public void onToServerRpcResponse(ToServerRpcResponseMsg msg) {
            responseWriter.setResult(new ResponseEntity<>(JsonConverter.toJson(msg).toString(), HttpStatus.OK));
        }

    }

    private static MediaType parseMediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @Override
    public String getName() {
        return DataConstants.HTTP_TRANSPORT_NAME;
    }

}
