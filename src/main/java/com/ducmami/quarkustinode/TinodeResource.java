package com.ducmami.quarkustinode;

import com.google.protobuf.ByteString;
import io.grpc.Metadata;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcClientUtils;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestQuery;
import pbx.*;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Path("/public/underlord")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class TinodeResource {

        @GrpcClient("tinode")
    MutinyNodeGrpc.MutinyNodeStub tinode;
//    @GrpcClient("tinode")
//    Node nodeStub;


    @GET
    @Path("/hello-grpc")
    public Multi<String> helloGrpc(@RestQuery String name) throws UnsupportedEncodingException {


        String encodedString = Base64.getEncoder().encodeToString("ducmami:123456".getBytes());
        String id = UUID.randomUUID().toString();
        Tinode.ClientHi clientHi = Tinode.ClientHi.newBuilder()
                .setId(id)
                .setUserAgent("Quarkus-GRPC/0.0.1")
                .setVer("0.18.2")
                .setLang("en-US")
                .setPlatform("android")
                .setDeviceId(id)
                .build();
        Tinode.ClientLogin clientLogin = Tinode.ClientLogin.newBuilder()
                .setId(id+"1")
                .setScheme("basic")
                .setSecret(ByteString.copyFrom("pockyjr:123456", "UTF-8"))
                .build();

        Tinode.ClientSub clientSub = Tinode.ClientSub.newBuilder()
                .setId(id+"2")
                .setTopic("grpN2f2eIxUihs")
                .setGetQuery(Tinode.GetQuery.newBuilder()
                        .setWhat("sub desc")
                        .build())
                .build();

        Tinode.ClientPub clientPub = Tinode.ClientPub.newBuilder()
                .setId(id+"3")
                .setTopic("grpN2f2eIxUihs")
                .setNoEcho(true)
//                .setContent(ByteString.copyFrom("{\"txt\":\"Send from Quarkus\"}", "UTF-8"))
                .setContent(ByteString.copyFrom("\"Send from Quarkus\"", "UTF-8"))
                .build();

        Tinode.ClientMsg msg1 = Tinode.ClientMsg.newBuilder()
                .setHi(clientHi)
                .build();
        Tinode.ClientMsg msg2 = Tinode.ClientMsg.newBuilder()
                .setLogin(clientLogin)
                .build();

        Tinode.ClientMsg msg3 = Tinode.ClientMsg.newBuilder()
                .setSub(clientSub)
                .build();
        Tinode.ClientMsg msg4 = Tinode.ClientMsg.newBuilder()
                .setPub(clientPub)
                .build();

        final ByteString nullByteString = ByteString.copyFrom("null", "UTF-8");
        Multi<Tinode.ClientMsg> requestMsg = Multi.createFrom().items(msg1, msg2, msg3,msg4)
                .onItem().call(i ->
                Uni.createFrom().nullItem().onItem().delayIt().by(Duration.ofMillis(1000))
        )
                ;
        Multi<Tinode.ClientMsg> requestMsg1 = Multi.createFrom().items(msg1);
        Multi<Tinode.ClientMsg> requestMsg2 = Multi.createFrom().items(msg2);

        Multi<Tinode.ServerMsg> serverMsgMulti = tinode.messageLoop(requestMsg.log());

        return serverMsgMulti
//                .flatMap(serverMsg -> {
//                    String data = serverMsg.getCtrl().getParamsOrDefault("user", nullByteString).toStringUtf8() + ":" + serverMsg.getCtrl().getParamsOrDefault("user", nullByteString).toStringUtf8();
//                    log.info(data);
//                    return tinode.messageLoop(requestMsg2.log());
//                })
//                .map(serverMsg -> {
//                    String data = serverMsg.getCtrl().getParamsOrDefault("user", nullByteString).toStringUtf8() + ":" + serverMsg.getCtrl().getParamsOrDefault("user", nullByteString).toStringUtf8();
//                    log.info(data);
//                    return data;
//                })

                .onItem()
                .transform(serverMsg -> {
                    log.info(serverMsg.toString());
                    String data =  serverMsg.getCtrl().getParamsOrDefault("user", nullByteString).toStringUtf8() + ":" + serverMsg.getCtrl().getParamsOrDefault("user", nullByteString).toStringUtf8();
                    return data;
                })
                                .onItem().call(i ->
                Uni.createFrom().nullItem().onItem().delayIt().by(Duration.ofMillis(1000))
        )
//                .log()
                ;
    }
}
