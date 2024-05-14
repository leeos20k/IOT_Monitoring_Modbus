package com.example.demo;

import com.example.demo.repository.ModbusRepository;
import com.opencsv.CSVWriter;
import lombok.extern.log4j.Log4j;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;



import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;





@Service
@Log4j
public class ModbusData {

    private static ModbusRepository repository = null;

    public ModbusData(ModbusRepository repository) {
        ModbusData.repository = repository;
    }

    //    @Scheduled(cron = "0 0/2 * * * *")
//    public static  void deldel1(){
//        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
//        ZonedDateTime now = nowSeoul.plusDays(4);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷
//        Map<String, Object> map = new HashMap<>();
//        map.put("pumpEventTm", nowSeoul.format(formatter)); //파라미터 맵에 저장시간(현재시간) put
//        map.put("slaveId", 7002);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
//        map.put("seq", 5);                                       //파라미터 맵에 i번째 슬레이브의 seq put
//        map.put("orgId", 1);                                   //전역변수 orgId
//        map.put("rValue", 20202);                                   //전류 슬레이브가 아니기 때문에 r s t Value 를 null 로 설정
//        map.put("sValue", 20202);
//        map.put("tValue", 20202);
//        map.put("lValue", 20202); //전류 슬레이브가 아닌 경우에는 array 에 값이 1개씩 저장되므로 앞의 1개 값을 lValue 에 매핑
//
//        for(int mu=1;mu<999999999;mu++){
//            repository.insertPump(map); //파라미터맵으로 쿼리수행 = DB에 데이터 저장
//            map.put("pumpEventTm", now.plusSeconds(mu).format(formatter));
//        }
//    }
//    @Scheduled(cron = "0 0/2 * * * *")
//    public static  void deldel2(){
//        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
//        ZonedDateTime now = nowSeoul.plusDays(14);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷
//        Map<String, Object> map = new HashMap<>();
//        map.put("pumpEventTm", nowSeoul.format(formatter)); //파라미터 맵에 저장시간(현재시간) put
//        map.put("slaveId", 7002);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
//        map.put("seq", 5);                                       //파라미터 맵에 i번째 슬레이브의 seq put
//        map.put("orgId", 1);                                   //전역변수 orgId
//        map.put("rValue", 20202);                                   //전류 슬레이브가 아니기 때문에 r s t Value 를 null 로 설정
//        map.put("sValue", 20202);
//        map.put("tValue", 20202);
//        map.put("lValue", 20202); //전류 슬레이브가 아닌 경우에는 array 에 값이 1개씩 저장되므로 앞의 1개 값을 lValue 에 매핑
//
//        for(int mu=1;mu<999999999;mu++){
//            repository.insertPump(map); //파라미터맵으로 쿼리수행 = DB에 데이터 저장
//            map.put("pumpEventTm", now.plusSeconds(mu).format(formatter));
//        }
//    }
//    @Scheduled(cron = "0 0/2 * * * *")
//    public static  void deldel3(){
//        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
//        ZonedDateTime now = nowSeoul.plusDays(24);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷
//        Map<String, Object> map = new HashMap<>();
//        map.put("pumpEventTm", nowSeoul.format(formatter)); //파라미터 맵에 저장시간(현재시간) put
//        map.put("slaveId", 7002);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
//        map.put("seq", 5);                                       //파라미터 맵에 i번째 슬레이브의 seq put
//        map.put("orgId", 1);                                   //전역변수 orgId
//        map.put("rValue", 20202);                                   //전류 슬레이브가 아니기 때문에 r s t Value 를 null 로 설정
//        map.put("sValue", 20202);
//        map.put("tValue", 20202);
//        map.put("lValue", 20202); //전류 슬레이브가 아닌 경우에는 array 에 값이 1개씩 저장되므로 앞의 1개 값을 lValue 에 매핑
//
//        for(int mu=1;mu<999999999;mu++){
//            repository.insertPump(map); //파라미터맵으로 쿼리수행 = DB에 데이터 저장
//            map.put("pumpEventTm", now.plusSeconds(mu).format(formatter));
//        }
//    }
//    @Scheduled(cron = "0 0/2 * * * *")
//    public static  void deldel4(){
//        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
//        ZonedDateTime now = nowSeoul.plusDays(34);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷
//        Map<String, Object> map = new HashMap<>();
//        map.put("pumpEventTm", nowSeoul.format(formatter)); //파라미터 맵에 저장시간(현재시간) put
//        map.put("slaveId", 7002);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
//        map.put("seq", 5);                                       //파라미터 맵에 i번째 슬레이브의 seq put
//        map.put("orgId", 1);                                   //전역변수 orgId
//        map.put("rValue", 20202);                                   //전류 슬레이브가 아니기 때문에 r s t Value 를 null 로 설정
//        map.put("sValue", 20202);
//        map.put("tValue", 20202);
//        map.put("lValue", 20202); //전류 슬레이브가 아닌 경우에는 array 에 값이 1개씩 저장되므로 앞의 1개 값을 lValue 에 매핑
//
//        for(int mu=1;mu<999999999;mu++){
//            repository.insertPump(map); //파라미터맵으로 쿼리수행 = DB에 데이터 저장
//            map.put("pumpEventTm", now.plusSeconds(mu).format(formatter));
//        }
//    }
//    @Scheduled(cron = "0 0/2 * * * *")
//    public static  void deldel5(){
//        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
//        ZonedDateTime now = nowSeoul.plusDays(44);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷
//        Map<String, Object> map = new HashMap<>();
//        map.put("pumpEventTm", nowSeoul.format(formatter)); //파라미터 맵에 저장시간(현재시간) put
//        map.put("slaveId", 7002);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
//        map.put("seq", 5);                                       //파라미터 맵에 i번째 슬레이브의 seq put
//        map.put("orgId", 1);                                   //전역변수 orgId
//        map.put("rValue", 20202);                                   //전류 슬레이브가 아니기 때문에 r s t Value 를 null 로 설정
//        map.put("sValue", 20202);
//        map.put("tValue", 20202);
//        map.put("lValue", 20202); //전류 슬레이브가 아닌 경우에는 array 에 값이 1개씩 저장되므로 앞의 1개 값을 lValue 에 매핑
//
//        for(int mu=1;mu<999999999;mu++){
//            repository.insertPump(map); //파라미터맵으로 쿼리수행 = DB에 데이터 저장
//            map.put("pumpEventTm", now.plusSeconds(mu).format(formatter));
//        }
//    }
//    @Scheduled(cron = "0 0/2 * * * *")
//    public static  void deldel6(){
//        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
//        ZonedDateTime now = nowSeoul.plusDays(54);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷
//        Map<String, Object> map = new HashMap<>();
//        map.put("pumpEventTm", nowSeoul.format(formatter)); //파라미터 맵에 저장시간(현재시간) put
//        map.put("slaveId", 7002);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
//        map.put("seq", 5);                                       //파라미터 맵에 i번째 슬레이브의 seq put
//        map.put("orgId", 1);                                   //전역변수 orgId
//        map.put("rValue", 20202);                                   //전류 슬레이브가 아니기 때문에 r s t Value 를 null 로 설정
//        map.put("sValue", 20202);
//        map.put("tValue", 20202);
//        map.put("lValue", 20202); //전류 슬레이브가 아닌 경우에는 array 에 값이 1개씩 저장되므로 앞의 1개 값을 lValue 에 매핑
//
//        for(int mu=1;mu<999999999;mu++){
//            repository.insertPump(map); //파라미터맵으로 쿼리수행 = DB에 데이터 저장
//            map.put("pumpEventTm", now.plusSeconds(mu).format(formatter));
//        }
//    }
//    @Scheduled(cron = "0 0/2 * * * *")
//    public static  void deldel7(){
//        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
//        ZonedDateTime now = nowSeoul.plusDays(64);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷
//        Map<String, Object> map = new HashMap<>();
//        map.put("pumpEventTm", nowSeoul.format(formatter)); //파라미터 맵에 저장시간(현재시간) put
//        map.put("slaveId", 7002);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
//        map.put("seq", 5);                                       //파라미터 맵에 i번째 슬레이브의 seq put
//        map.put("orgId", 1);                                   //전역변수 orgId
//        map.put("rValue", 20202);                                   //전류 슬레이브가 아니기 때문에 r s t Value 를 null 로 설정
//        map.put("sValue", 20202);
//        map.put("tValue", 20202);
//        map.put("lValue", 20202); //전류 슬레이브가 아닌 경우에는 array 에 값이 1개씩 저장되므로 앞의 1개 값을 lValue 에 매핑
//
//        for(int mu=1;mu<999999999;mu++){
//            repository.insertPump(map); //파라미터맵으로 쿼리수행 = DB에 데이터 저장
//            map.put("pumpEventTm", now.plusSeconds(mu).format(formatter));
//        }
//    }
//    @Scheduled(cron = "0 0/2 * * * *")
//    public static  void deldel8(){
//        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
//        ZonedDateTime now = nowSeoul.plusDays(74);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷
//        Map<String, Object> map = new HashMap<>();
//        map.put("pumpEventTm", nowSeoul.format(formatter)); //파라미터 맵에 저장시간(현재시간) put
//        map.put("slaveId", 7002);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
//        map.put("seq", 5);                                       //파라미터 맵에 i번째 슬레이브의 seq put
//        map.put("orgId", 1);                                   //전역변수 orgId
//        map.put("rValue", 20202);                                   //전류 슬레이브가 아니기 때문에 r s t Value 를 null 로 설정
//        map.put("sValue", 20202);
//        map.put("tValue", 20202);
//        map.put("lValue", 20202); //전류 슬레이브가 아닌 경우에는 array 에 값이 1개씩 저장되므로 앞의 1개 값을 lValue 에 매핑
//
//        for(int mu=1;mu<999999999;mu++){
//            repository.insertPump(map); //파라미터맵으로 쿼리수행 = DB에 데이터 저장
//            map.put("pumpEventTm", now.plusSeconds(mu).format(formatter));
//        }
//    }
//    @Scheduled(cron = "0 0/2 * * * *")
//    public static  void deldel9(){
//        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
//        ZonedDateTime now = nowSeoul.plusDays(84);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷
//        Map<String, Object> map = new HashMap<>();
//        map.put("pumpEventTm", nowSeoul.format(formatter)); //파라미터 맵에 저장시간(현재시간) put
//        map.put("slaveId", 7002);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
//        map.put("seq", 5);                                       //파라미터 맵에 i번째 슬레이브의 seq put
//        map.put("orgId", 1);                                   //전역변수 orgId
//        map.put("rValue", 20202);                                   //전류 슬레이브가 아니기 때문에 r s t Value 를 null 로 설정
//        map.put("sValue", 20202);
//        map.put("tValue", 20202);
//        map.put("lValue", 20202); //전류 슬레이브가 아닌 경우에는 array 에 값이 1개씩 저장되므로 앞의 1개 값을 lValue 에 매핑
//
//        for(int mu=1;mu<999999999;mu++){
//            repository.insertPump(map); //파라미터맵으로 쿼리수행 = DB에 데이터 저장
//            map.put("pumpEventTm", now.plusSeconds(mu).format(formatter));
//        }
//    }
//    @Scheduled(cron = "0 0/2 * * * *")
//    public static  void deldel10(){
//        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
//        ZonedDateTime now = nowSeoul.plusDays(94);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷
//        Map<String, Object> map = new HashMap<>();
//        map.put("pumpEventTm", nowSeoul.format(formatter)); //파라미터 맵에 저장시간(현재시간) put
//        map.put("slaveId", 7002);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
//        map.put("seq", 5);                                       //파라미터 맵에 i번째 슬레이브의 seq put
//        map.put("orgId", 1);                                   //전역변수 orgId
//        map.put("rValue", 20202);                                   //전류 슬레이브가 아니기 때문에 r s t Value 를 null 로 설정
//        map.put("sValue", 20202);
//        map.put("tValue", 20202);
//        map.put("lValue", 20202); //전류 슬레이브가 아닌 경우에는 array 에 값이 1개씩 저장되므로 앞의 1개 값을 lValue 에 매핑
//
//        for(int mu=1;mu<999999999;mu++){
//            repository.insertPump(map); //파라미터맵으로 쿼리수행 = DB에 데이터 저장
//            map.put("pumpEventTm", now.plusSeconds(mu).format(formatter));
//        }
//    }
//    @Scheduled(cron = "* 29 14 * * *")
    public static  void deldel11(){
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
        ZonedDateTime now = nowSeoul.plusYears(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷


        for(int mu=1;mu<999999999;mu++){
            Map<String, Object> map = new HashMap<>();
            map.put("pumpEventTm", now.plusSeconds(mu).format(formatter)); //파라미터 맵에 저장시간(현재시간) put
            map.put("slaveId", 7002);                               //파라미터 맵에 i번째 슬레이브의 slaveId put
            map.put("seq", 5);                                       //파라미터 맵에 i번째 슬레이브의 seq put
            map.put("orgId", 1);                                   //전역변수 orgId
            map.put("rValue", (int)(Math.random()*100));                                   //전류 슬레이브가 아니기 때문에 r s t Value 를 null 로 설정
            map.put("sValue", (int)(Math.random()*100));
            map.put("tValue", (int)(Math.random()*100));
            map.put("lValue", (int)(Math.random()*100)); //전류 슬레이브가 아닌 경우에는 array 에 값이 1개씩 저장되므로 앞의 1개 값을 lValue 에 매핑
            repository.insertPump(map); //파라미터맵으로 쿼리수행 = DB에 데이터 저장
        }
    }


    //    @Scheduled(cron = "0 13 9 * * *") // <- 동작 x
    public static void dataSaveDelete() {

        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("sdTime", nowSeoul.format(formatter)); //쿼리수행을 위한 파라미터 맵 설정

        List<Map<String, Object>> saveList = repository.saveData(paramsMap); //스케줄이 발생한 시간(yyyy-MM-dd HH:mm:ss)을 기준으로 쿼리 조회데이터 저장

        //데이터저장*************************************************************************************************************************************
        String csvFilePath = "C:\\csv\\csvDATA300m.csv"; // CSV 파일 경로 <- 해당 경로에 폴더가 있어야 한다. 경로 폴더는 자동생성 x
        // 데이터를 추가하는 경우, 이 로직이 수행될 때 해당 파일을 켜놓으면 데이터 추가 x
        // String fileName = nowSeoul.format(formatter) + ".csv"; // 파일이름을 동적으로(시간) 설정하고자 하는 경우, 새로운 Formatter 선언해야 함.
        // String csvFilePath = "C:\\csv\\" + fileName;           // yyyy-MM-dd HH:mm:ss <- 띄어쓰기, ':' 은 파일이름으로 사용 불가.

        File csvFile = new File(csvFilePath); //앞서 설정한 CSV 파일을 나타내는 객체
        boolean fileExists = csvFile.exists() && csvFile.length() > 0; //해당 파일이 존재하는지, 데이터가 있는지 확인

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath, true))) { //해당 경로에 CSV 파일 생성할 것, 데이터 append 되도록 허용
            // 헤더 쓰기
            Map<String, Object> firstMap = saveList.get(0); //saveList 의 첫 번째 맵
            String[] headers = firstMap.keySet().toArray(new String[0]); // firstMap 의 키 집합을 배열로 변환, 초기 크기가 0인 String 배열을 toArray 로 크기조정
            // 첫 번째 맵의 key 값을 header(column)로 설정 <- 모든 맵의 key 는 동일하므로
            if (!fileExists) {
                writer.writeNext(headers); // fileExists 가 false <- 앞선 데이터가 없을때만 헤더를 추가
            }
            // csvDATA.csv
            // ---------------헤더----------------
            // ---------------데이터--------------
            // ---------------헤더----------------
            // ---------------데이터--------------
            // ---------------헤더----------------
            // ---------------데이터--------------    <- 데이터가 이렇게 쌓이는 것을 방지
            // row 쓰기
            for (Map<String, Object> map : saveList) { // saveList 의 모든 맵 데이터 차례대로 수행
                String[] row = new String[headers.length]; //헤더의 길이와 동일한 길이의 문자열 형태 배열 선언
                for (int i = 0; i < headers.length; i++) { //한 row 의 각 데이터 형변환 (CSV 파일은 String 과 ',' 로 이루어지므로)
                    Object value = map.get(headers[i]);
                    if (value == null) {
                        row[i] = "null"; // null 값 처리
                    } else {
                        row[i] = String.valueOf(value); // null 이 아니면 String 으로 변환
                    }
                }
                writer.writeNext(row); // row 데이터 추가
            }
            log.info("CSV 파일이 생성되었습니다.");
        } catch (Exception e) {
            log.warn("CSV 파일 생성 중 에러 발생 : " + e);
        }//*******************************************************************************************************************************************

        //데이터 삭제
        //repository.deleteData(map);

    }

//    @Scheduled(cron = "0 17 17 * * *")
//    public static <RootAllocator, VectorSchemaRoot> void saveParquet() throws IOException {
//        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));//현재시각
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//시간 포맷
//
//        Map<String, Object> paramsMap = new HashMap<>();
//        paramsMap.put("sdTime", nowSeoul.format(formatter)); //쿼리수행을 위한 파라미터 맵 설정
//
//        List<Map<String, Object>> saveList = repository.saveData(paramsMap); //스케줄이 발생한 시간(yyyy-MM-dd HH:mm:ss)을 기준으로 쿼리 조회데이터 저장
//
//        System.out.println("saveList :"+saveList);
//
//
//        String path = "c:/Users/Swontech/Untitled Folder/test.parquet";
//        Schema schema = Schema.createRecord(Arrays.asList(
//                new Schema.Field("a", Schema.create(Schema.Type.INT), null, null),
//                new Schema.Field("b", Schema.create(Schema.Type.STRING), null, null),
//                new Schema.Field("c", Schema.create(Schema.Type.STRING), null, null)));
//
//        try (AvroParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(new Path(path))
//                .withSchema(schema)
//                .withCompressionCodec(CompressionCodecName.SNAPPY)
//                .build()) {
//
//            for (Map<String, Object> record : saveList) {
//                GenericRecord avroRecord = new GenericData.Record(schema);
//
//                for (Map.Entry<String, Object> entry : record.entrySet()) {
//                    avroRecord.put(entry.getKey(), entry.getValue());
//                }
//
//                writer.write(avroRecord);
//            }
//        }
//
//        ParquetWriter writer = new ParquetWriter();
//        writer.writeParquet("/path/to/parquet/file", records);
//    }

}
