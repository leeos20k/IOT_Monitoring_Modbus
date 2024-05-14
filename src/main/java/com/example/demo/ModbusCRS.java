package com.example.demo;


import com.example.demo.repository.ModbusRepository;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import lombok.extern.log4j.Log4j;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Log4j
public class ModbusCRS {//Connect, Read, save

    public static int orgId = 1;
    private static ModbusRepository repository = null;

    public ModbusCRS(ModbusRepository repository) {
        ModbusCRS.repository = repository;
    }

    public static ModbusTCPMaster Connect(String masterIp, int masterId, int portNo, String socketMth, boolean reconnect, int timeout) {
//        ModbusTCPMaster modbusTCPMaster = new ModbusTCPMaster(masterIp, portNo);

        ModbusTCPMaster modbusTCPMaster = null;
        try {
            if(socketMth.equals("TCP")){
                modbusTCPMaster = new ModbusTCPMaster(masterIp, portNo, timeout, reconnect, false);
            }else if(socketMth.equals("RTU")){
                modbusTCPMaster = new ModbusTCPMaster(masterIp, portNo, timeout, reconnect, true);
            }
            if(modbusTCPMaster != null){
                modbusTCPMaster.connect();
            }
            log.info("masterIp:" + masterIp + ", portNo:" + portNo + " 연결 성공!");
        } catch (Exception e) {
            log.info("masterIp:" + masterIp + ", portNo:" + portNo + " 연결 실패");
            //해당 마스터에 속한 모든 slave seq에 에러상태 업데이트

            Map<String, Object> unitMap = new HashMap<>();
            unitMap.put("orgId", orgId);
            unitMap.put("masterId", masterId);
            List<Map<String, Object>> unitList = repository.getSlaveUnitIDInfoVO(unitMap);
            for (int q = 0; q < unitList.size(); q++) {
                int slaveId = 0;
                if (unitList.get(q).get("slaveId") != null){
                    slaveId = Integer.parseInt(String.valueOf(unitList.get(q).get("slaveId")));
                }
                List<Map<String, Object>> slaveDetailList = repository.GetSlaveSeqInfoVO(slaveId);
                for(int i=0; i<slaveDetailList.size(); i++){
                    int seq=0;
                    if(slaveDetailList.get(i).get("seq")!=null){
                        seq = Integer.parseInt(String.valueOf(slaveDetailList.get(i).get("seq")));
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("slaveId",slaveId);
                    map.put("seq",seq);
                    repository.updateCommunicationError(map);
                }
            }
            return null;
        }

        return modbusTCPMaster;
    }


    public static ArrayList Read(ModbusTCPMaster modbusTCPMaster, int masterId, String masterIp) {
        Map<String, Object> unitMap = new HashMap<>();
        unitMap.put("orgId", orgId);
        unitMap.put("masterId", masterId);
        List<Map<String, Object>> unitList = repository.getSlaveUnitIDInfoVO(unitMap);

        ArrayList<Integer> array = new ArrayList<>();


        System.out.println("print : "+unitList+ "masterIp : "+masterIp);



        for (int q = 0; q < unitList.size(); q++) {
            int slaveId = 0;
            String register = "";
            int startNum = 0;
            int slaveCnt = 0;
            int slaveUnitId = 0;
            if (unitList.get(q).get("slaveId") != null && unitList.get(q).get("register") != null && unitList.get(q).get("startNum") != null &&
                    unitList.get(q).get("slaveCnt") != null && unitList.get(q).get("slaveUnitId") != null) {
                slaveId = Integer.parseInt(String.valueOf(unitList.get(q).get("slaveId")));
                register = String.valueOf(unitList.get(q).get("register"));
                startNum = Integer.parseInt(String.valueOf(unitList.get(q).get("startNum")));
                slaveCnt = Integer.parseInt(String.valueOf(unitList.get(q).get("slaveCnt")));
                slaveUnitId = Integer.parseInt(String.valueOf(unitList.get(q).get("slaveUnitId")));
            }

            System.out.println("masterIp:"+masterIp+" slaveId:"+slaveId+" register:"+register+" startNum:"+startNum+" slaveCnt:"+slaveCnt+" slaveUnitId:"+slaveUnitId);
            try {
                switch (register) {
                    case "Holding":
                        startNum -= 40001;
                        Register[] HoldingRegisterData = modbusTCPMaster.readMultipleRegisters(slaveUnitId, startNum, slaveCnt);
                        for (Register data : HoldingRegisterData) {
                            int value = data.getValue();
                            if (value >= 65000 && value <= 65536) {//마이너스(-1 = 65535)가 나오면 값 변환
                                array.add((65536 - value) * -1);
                            } else {
                                array.add(value);//원래 이 한줄
                            }
                        }
                        break;
                    case "Input":
                        startNum -= 30001;
                        InputRegister[] InputRegisterData = modbusTCPMaster.readInputRegisters(slaveUnitId, startNum, slaveCnt);
                        for (InputRegister data : InputRegisterData) {
                            int value = data.getValue();
                            if (value >= 65000 && value <= 65536) {//마이너스(-1 = 65535)가 나오면 값 변환
                                array.add((65536 - value) * -1);
                            } else {
                                array.add(value);//원래 이 한줄
                            }
                        }
                        break;
                }
            } catch (ModbusException e) {
                log.warn("masterIp:" + masterIp + " slaveId:" + slaveId + " Register read 중 에러 발생:" + e);
                //값을 못가져와서 에러발생시, slaveCnt 만큼 -99999추가
                for (int a = 0; a < slaveCnt; a++) {
                    array.add(-99999);
                }
                modbusTCPMaster.disconnect();
            }
        }

//        for (int i = 0; i < unitList.size(); i++) {
//            int o = Integer.parseInt((String.valueOf(unitList.get(i).get("slaveCnt"))));
//            for (int j = 0; j < o; j++) {
//                if (masterIp.equals("192.168.10.99")) {
//                    array.add(500);
//                } else if (masterIp.equals("192.168.10.10")) {
//                    array.add(43);
//                } else if (masterIp.equals("192.168.20.21")) {
//                    array.add(45);
//                } else if (masterIp.equals("255.255.255.0")){
//                    array.add(23);
//                } else if (masterIp.equals("0.0.0.0")) {
//                    array.add(24);
//                }  else {
//                    array.add(50);
//                }
//            }
//        }
        log.info("array*******************:"+array);
        return array;
    }


    public static void Save(ArrayList<Integer> readArray, String masterIp, int masterId, int portNo, List<Map<String, Object>> alarmMaps) {//readArray 에 저장되어있는 값을 분배하여 DB에 insert

        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷

        Map<String, Object> unitMap = new HashMap<>();
        unitMap.put("orgId", orgId);
        unitMap.put("masterId", masterId);
        unitMap.put("masterIp", masterIp);
        unitMap.put("portNo", portNo);
        List<Map<String, Object>> slaveAlarmList = repository.getSlaveAlarm(unitMap);
        List<Map<String, Object>> unitList = repository.getSlaveUnitIDInfoVO(unitMap);

        int cnt=0;

        for (int q = 0; q < unitList.size(); q++) {
            int slaveId = 0;
            int slaveStartNum = 0;
            int slaveAllCnt = 0;
            if (unitList.get(q).get("slaveId") != null && unitList.get(q).get("startNum") != null && unitList.get(q).get("slaveCnt") != null){
                slaveId = Integer.parseInt(String.valueOf(unitList.get(q).get("slaveId")));
                slaveStartNum = Integer.parseInt(String.valueOf(unitList.get(q).get("startNum")));
                slaveAllCnt = Integer.parseInt(String.valueOf(unitList.get(q).get("slaveCnt")));
            }
            List<Integer> slaveArray = new ArrayList<>(readArray.subList(0, slaveAllCnt));
            readArray.subList(0, slaveAllCnt).clear();
            List<Map<String, Object>> slaveDetailList = repository.GetSlaveSeqInfoVO(slaveId);

            for(int i=0; i<slaveDetailList.size(); i++){
                int seq=0;
                int startNum=0;
                String slaveTp="";
                String useFlag="";
                String alarmTp=null;
                String alarmEndTm=null;
                if(slaveDetailList.get(i).get("seq")!=null && slaveDetailList.get(i).get("startNum")!=null
                        && slaveDetailList.get(i).get("slaveTp")!=null && slaveDetailList.get(i).get("useFlag")!=null) {
                    seq = Integer.parseInt(String.valueOf(slaveDetailList.get(i).get("seq")));
                    startNum = Integer.parseInt(String.valueOf(slaveDetailList.get(i).get("startNum")));
                    slaveTp = String.valueOf(slaveDetailList.get(i).get("slaveTp"));
                    useFlag = String.valueOf(slaveDetailList.get(i).get("useFlag"));
                }

                try {
                    Map<String, Object> map = new HashMap<>();
                    if (slaveTp.equals("A")) {//전류 슬레이브인 경우
                        map.put("pumpEventTm", nowSeoul.format(formatter)); //파라미터 맵에 저장시간(현재시간) put
                        map.put("slaveId", slaveId);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
                        map.put("seq", seq);                                       //파라미터 맵에 i번째 슬레이브의 seq put
                        map.put("orgId", orgId);                                   //전역변수 orgId
                        map.put("rValue", slaveArray.get(startNum - slaveStartNum)); //전류 슬레이브는 array 에 값이 3개씩 저장되므로 앞의 3개 값을 r s t Value 에 매핑
                        map.put("sValue", slaveArray.get(startNum - slaveStartNum + 1));
                        map.put("tValue", slaveArray.get(startNum - slaveStartNum + 2));
                        map.put("lValue", null);         //전류 슬레이브이므로 lValue 는 null
                    } else {//전류 슬레이브가 아닌경우(슬레이브 데이터가 단일 값. cnt=1)
                        map.put("pumpEventTm", nowSeoul.format(formatter)); //파라미터 맵에 저장시간(현재시간) put
                        map.put("slaveId", slaveId);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
                        map.put("seq", seq);                                       //파라미터 맵에 i번째 슬레이브의 seq put
                        map.put("orgId", orgId);                                   //전역변수 orgId
                        map.put("rValue", null);                                   //전류 슬레이브가 아니기 때문에 r s t Value 를 null 로 설정
                        map.put("sValue", null);
                        map.put("tValue", null);
                        map.put("lValue", slaveArray.get(startNum - slaveStartNum)); //전류 슬레이브가 아닌 경우에는 array 에 값이 1개씩 저장되므로 앞의 1개 값을 lValue 에 매핑
                    }
                    if (useFlag.equals("Y")) {//useFlag 가 Y일 때만 아래 내용을 수행
                        //데이터 넣기전에 체크. -99999 값이 들어왔다면
                        if (slaveArray.get(startNum - slaveStartNum) == -99999) {
                            //해당 슬레이브 seq 에러났다고 업데이트
                            repository.updateCommunicationError(map);
                        } else {
                            repository.insertPump(map); //파라미터맵으로 쿼리수행 = DB에 데이터 저장
                            if (slaveDetailList.get(i).get("alarmTp") != null && slaveDetailList.get(i).get("alarmEndTm") != null) {



                                log.warn("alarmTp:" + slaveDetailList.get(i).get("alarmTp") + " alarmEndTm:" + slaveDetailList.get(i).get("alarmEndTm"));
                                alarmEndTm = String.valueOf(slaveDetailList.get(i).get("alarmEndTm"));
                                DateTimeFormatter add10Formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S").withZone(ZoneId.of("Asia/Seoul"));
                                ZonedDateTime add10Time = ZonedDateTime.parse(alarmEndTm, add10Formatter);
                                ZonedDateTime alarmEndTmPlus10Min = add10Time.plusMinutes(10);
                                if (nowSeoul.isBefore(alarmEndTmPlus10Min)) {
                                    map.put("alarmEndTm", slaveDetailList.get(i).get("alarmEndTm"));
                                    repository.insertSlave031(map);
                                }




                            }
                            map.put("slaveTp", slaveTp);
                            map.put("useFlag", useFlag);

                            ModbusAlarm modbusAlarm = new ModbusAlarm(repository);
                            try {//현재 시점의 슬레이브 데이터 값을 참조하여, 알람 발생여부를 판별.
//                        log.warn("cnt="+cnt + "q="+q+ " i="+i+" alarmMaps.size()"+alarmMaps.size()+"  slaveAlarmList.size():"+slaveAlarmList.size());
                                modbusAlarm.alarmCheck(map, alarmMaps.get(cnt), slaveAlarmList.get(cnt), nowSeoul); //맵, 알람맵, 시간정보를 담아서 alarmCheck
                                cnt += 1;
                            } catch (Exception e) {
                                log.error("알람체크 부분 오류 발생 : " + e);
                            } finally {
                                modbusAlarm = null;
                            }
                        }
                    }
                }catch (Exception e){
                    log.warn("기타 에러 발생 : "+e);
                    Map<String, Object> etcMap = new HashMap<>();
                    etcMap.put("slaveId",slaveId);
                    etcMap.put("seq",seq);
                    repository.updateEtcError(etcMap);
                }
            }
        }





//        for (int i = 0; i < searchList.size(); i++) {//해당 마스터에 있는 슬레이브개수만큼 반복
//            int slaveId = Integer.parseInt(String.valueOf(searchList.get(i).get("slaveId")));//i번째 슬레이브의 slaveId
//            int seq = Integer.parseInt(String.valueOf(searchList.get(i).get("seq")));//i번째 슬레이브의 seq
//            int alarmMaxValue = 0;
//            int alarmMaxLValue = 0;
//            int alarmMinHValue = 0;
//            int alarmMinValue = 0;
//            int slaveBaseValue = 0;
//            int phaseUnbRate = 0;
//            int alarmTm = 0;
//            int slaveAllCnt = 0;
//            if (searchList.get(i).get("alarmMaxValue") != null && searchList.get(i).get("alarmMaxLValue") != null && searchList.get(i).get("alarmMinHValue") != null
//                && searchList.get(i).get("alarmMinValue") != null && searchList.get(i).get("slaveBaseValue") != null && searchList.get(i).get("phaseUnbRate") != null
//                 && searchList.get(i).get("alarmTm") != null && searchList.get(i).get("slaveAllCnt") != null) {
//                alarmMaxValue = Integer.parseInt(String.valueOf(searchList.get(i).get("alarmMaxValue")));//i번째 슬레이브의
//                alarmMaxLValue = Integer.parseInt(String.valueOf(searchList.get(i).get("alarmMaxLValue")));//i번째 슬레이브의
//                alarmMinHValue = Integer.parseInt(String.valueOf(searchList.get(i).get("alarmMinHValue")));//i번째 슬레이브의
//                alarmMinValue = Integer.parseInt(String.valueOf(searchList.get(i).get("alarmMinValue")));//i번째 슬레이브의
//                slaveBaseValue = Integer.parseInt(String.valueOf(searchList.get(i).get("slaveBaseValue")));//i번째 슬레이브의
//                phaseUnbRate = Integer.parseInt(String.valueOf(searchList.get(i).get("phaseUnbRate")));//i번째 슬레이브의
//                alarmTm = Integer.parseInt(String.valueOf(searchList.get(i).get("alarmTm")));//i번째 슬레이브의 알람 발생 기준시간
//                slaveAllCnt = Integer.parseInt(String.valueOf(searchList.get(i).get("slaveAllCnt")));//i번째 슬레이브에서 읽을 전체갯수
//            }
//            String useFlag = (String) searchList.get(i).get("useFlag");//i번째 슬레이브의 사용여부
//            String slaveTp = (String) searchList.get(i).get("slaveTp");//i번째 슬레이브의 타입
//
//
//            List<Integer> slaveArray = new ArrayList<>(readArray.subList(0, slaveAllCnt));
//            readArray.subList(0, slaveAllCnt).clear();
//            List<Map<String, Object>> slaveDetailList = repository.GetSlaveSeqInfoVO(slaveId);
//
//            for(int q=0; q<slaveDetailList.size(); q++){
//                int seq=0;
//                int startNum=0;
//                double waterRate=0;
//                String slaveTp="";
//                double tValue=0;
//                if(slaveDetailList.get(i).get("seq")!=null && slaveDetailList.get(i).get("startNum")!=null && slaveDetailList.get(i).get("waterRate")!=null &&
//                        slaveDetailList.get(i).get("slaveTp")!=null) {
//                    seq = Integer.parseInt(String.valueOf(slaveDetailList.get(i).get("seq")));
//                    startNum = Integer.parseInt(String.valueOf(slaveDetailList.get(i).get("startNum")));
//                    waterRate = Double.parseDouble(String.valueOf(slaveDetailList.get(i).get("waterRate")));
//                    slaveTp = String.valueOf(slaveDetailList.get(i).get("slaveTp"));
//                    tValue = Double.parseDouble(String.valueOf(slaveDetailList.get(i).get("tValue")));
//                }
//                Map<String, Object> insertMap = new HashMap<>();
//            }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//            Map<String, Object> map = new HashMap<>();//DB에 저장하기 위한 파라미터 맵 생성
//
//            //알람 맵을 설정할 때와, array 에 담아진 값을 저장할 때, 알람여부를 판별할 때, '같은 getSlaveAlarm 쿼리를 사용해서 맵핑을 하기 때문에',
//            //세가지 부분이 각각 반복문을 수행하더라도 '순서가 맞아서' 정상적으로 동작하는 상태
//
//            System.out.println("*************************************************************************");
//
//            if(slaveTp.equals("A")) {//전류 슬레이브인 경우
//                System.out.println("111111111111111111111111111111111");
//                map.put("pumpEventTm", nowSeoul.plusYears(1).format(formatter)); //파라미터 맵에 저장시간(현재시간) put
//                map.put("slaveId", slaveId);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
//                map.put("seq", seq);                                       //파라미터 맵에 i번째 슬레이브의 seq put
//                map.put("orgId", orgId);                                   //전역변수 orgId
//                System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+masterIp+" readArray:"+readArray);
//
//                map.put("rValue", readArray.get(0)); //전류 슬레이브는 array 에 값이 3개씩 저장되므로 앞의 3개 값을 r s t Value 에 매핑
//                map.put("sValue", readArray.get(1));
//                map.put("tValue", readArray.get(2));
//                System.out.println("222222222222222222222222222222222222");
//                if (readArray.get(0) == -9999) { //array 에 담긴 값이 -9999인 경우는 Read 부분에서 수행하지 못했을때를 표시하기 위해 하드코딩된 것이므로
//                    map.put("rValue", null);     //r s t Value 를 null 로 설정
//                    map.put("sValue", null);
//                    map.put("tValue", null);
//                }
//                System.out.println("333333333333333333333333333333333");
//                map.put("lValue", null);         //전류 슬레이브이므로 lValue 는 null
//                for (int m = 0; m < 3; m++) {    //저장한 값이 3개이므로
//                    readArray.remove(0);   //저장한 값은 array 에서 삭제. 이렇게 해야 다음루프의 readArray.get(0)값이 다른 슬레이브의 데이터로 매핑된다.
//                }
//            }else{//전류 슬레이브가 아닌경우(슬레이브 데이터가 단일 값. cnt=1)
//                System.out.println("77777777777777777777");
//                map.put("pumpEventTm", nowSeoul.plusYears(1).format(formatter)); //파라미터 맵에 저장시간(현재시간) put
//                map.put("slaveId", slaveId);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
//                map.put("seq", seq);                                       //파라미터 맵에 i번째 슬레이브의 seq put
//                map.put("orgId", orgId);                                   //전역변수 orgId
//                map.put("rValue", null);                                   //전류 슬레이브가 아니기 때문에 r s t Value 를 null 로 설정
//                map.put("sValue", null);
//                map.put("tValue", null);
//                map.put("lValue", readArray.get(0)); //전류 슬레이브가 아닌 경우에는 array 에 값이 1개씩 저장되므로 앞의 1개 값을 lValue 에 매핑
//                System.out.println("8888888888888888888888");
//                if (readArray.get(0) == -9999) {  //array 에 담긴 값이 -9999인 경우는 Read 부분에서 수행하지 못했을때를 표시하기 위해 하드코딩된 것이므로
//                    map.put("lValue", null);      //lValue 를 null로 설정
//                }
//                System.out.println("99999999999999999999999999999");
//                readArray.remove(0);        //저장한 값은 array 에서 삭제. 이렇게 해야 다음루프의 readArray.get(0)값이 다른 슬레이브의 데이터로 매핑된다.
//            }
//
//            //파라미터맵에 해당 슬레이브의 정보가 담아진 상태.
//
//            if (useFlag.equals("Y")) {//useFlag 가 Y일 때만 아래 내용을 수행
//                repository.insertPump(map); //파라미터맵으로 쿼리수행 = DB에 데이터 저장
//
////                ModbusAlarm modbusAlarm = new ModbusAlarm(repository);
////                try {//현재 시점의 슬레이브 데이터 값을 참조하여, 알람 발생여부를 판별.
////                    map.put("roofNum", i);
////                    map.put("useFlag", useFlag);
////                    map.put("slaveTp", slaveTp);
////                    map.put("maxValue", maxValue);
////                    map.put("minValue", minValue);
////                    map.put("alarmTm", alarmTm); //기존에 선언한 파라미터맵에 데이터 추가
////
////                    modbusAlarm.alarmCheck(map, alarmMaps, nowSeoul, formatter); //맵, 알람맵, 시간정보를 담아서 alarmCheck
////                } catch (Exception e) {
////                    log.error("알람체크 부분 오류 발생 : " + e);
////                } finally {
////                    modbusAlarm = null;
////                }
//            }
//        }





    }
}
