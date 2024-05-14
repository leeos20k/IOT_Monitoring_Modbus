package com.example.demo;

import com.example.demo.repository.ModbusRepository;
import lombok.extern.log4j.Log4j;

import java.util.*;

import static com.example.demo.ModbusMain.modbus;


@Log4j
public class ModbusTimer {

    public static int orgId = 1;
    private static ModbusRepository repository = null;
    public ModbusTimer(ModbusRepository repository) {
        ModbusTimer.repository = repository;
    }

    public static void startTimer(List<List<Map<String, Object>>> alarmMapsList){//전체 마스터가 각각 다른 간격으로 데이터를 수집하도록 타이머를 설정
        try {
            List<Map<String, Object>> list = repository.getMasterIPInfoVO(orgId);//등록되어있는 마스터 정보 조회, 저장
            for (int i = 0; i < list.size(); i++) {//리스트의 사이즈 만큼 = 마스터 한줄한줄에 대한 작업을 수행
                int period = Integer.parseInt(String.valueOf(list.get(i).get("collInterval"))) * 1000;//각 마스터마다 가지고 있는 수행간격 데이터((ex) period 3000 = 3초)
                int finalI = i;
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            modbus(list.get(finalI), alarmMapsList.get(finalI), timer);//한 마스터에 대하여, 그 마스터에 해당하는 알람정보 리스트를 참조시키고, 지금 수행시키는 타이머 객체의 주소를 참조시켜 모드버스실행
                        } catch (Exception e) {                                        //               ex) alarmMapsList 0: <- 조회되는 마스터 첫 번째에 해당
                            throw new RuntimeException(e);
                        }
                    }
                };
                timer.scheduleAtFixedRate(task, 1000, period);// 1000ms 후에 period 간격으로 task 실행
                //ex) [timer1 period:3]-1000ms-[timer2 period:5]-1000ms-[timer3 period:2]-1000ms-[timer4 period:10] ~~~
            }
            log.info("최초 Timer "+list.size()+"개가 정상적으로 실행됨");
        }catch(Exception e){
            log.error("최초 Timer 실행 중 오류발생 : "+e);
        }
    }


//    public static void restartTimer(Optional<Map<String, Object>> option, List<Map<String, Object>> alarmMaps) {//타이머를 재시작 할 때 수행, 파라미터 형태만 다르고 내용은 위와 같음.
//        try {
//            int period = Integer.parseInt(String.valueOf(option.get().get("collInterval"))) * 1000;
//            Timer timer = new Timer();
//            TimerTask task = new TimerTask() {
//                @Override
//                public void run() {
//                    try {
//                        modbus(option.get(), alarmMaps, timer);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            };
//            // 1000ms 후에 period 간격으로 task 실행
//            timer.scheduleAtFixedRate(task, 1000, period);
//            log.info("재실행 Timer 정상적으로 실행됨");
//        }catch (Exception e){
//            log.error("재실행 Timer 실행 중 오류발생 : "+e);
//        }
//    }


}
