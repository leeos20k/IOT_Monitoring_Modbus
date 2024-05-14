package com.example.demo.repository;

import lombok.extern.log4j.Log4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j
@Repository
public class ModbusRepository {
    private final SqlSessionTemplate sqlSessionTemplate;

    public ModbusRepository(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }


    public List<Map<String, Object>> getMasterIPInfoVO(int orgId) {
        return sqlSessionTemplate.selectList("Modbus.GetMasterIPInfoVO", orgId);
    }
    public List<Map<String, Object>> getSlaveAlarm(Map<String, Object> map) {
        return sqlSessionTemplate.selectList("Modbus.GetSlaveAlarm", map);
    }
    public List<Map<String, Object>> getSlaveUnitIDInfoVO(Map<String, Object> map){
        return sqlSessionTemplate.selectList("Modbus.GetSlaveUnitIDInfoVO",map);
    }
    public List<Map<String, Object>> GetSlaveSeqInfoVO(int slaveId) {
        return sqlSessionTemplate.selectList("Modbus.GetSlaveSeqInfoVO", slaveId);
    }
    public int insertPump(Map<String, Object> map) {
        return sqlSessionTemplate.insert("Modbus.InsertPump", map);
    }
    public int insertAlarm(Map<String, Object> map) {
        return sqlSessionTemplate.insert("Modbus.InsertAlarm", map);
    }

    public int updateAlarmOccurrence(Map<String, Object> map) { return sqlSessionTemplate.update("Modbus.updateAlarmOccurrenceVO", map);}

    public int updateCommunicationError(Map<String, Object> map) { return sqlSessionTemplate.update("Modbus.updateCommunicationErrorVO", map);}

    public int updateEtcError(Map<String, Object> map) { return sqlSessionTemplate.update("Modbus.updateEtcErrorVO", map);}

    public List<Map<String, Object>> getSlaveBeforeValue(Map<String, Object> map) {
        return sqlSessionTemplate.selectList("Modbus.GetSlaveBeforeValueVO", map);
    }

    public int insertSlave031List(List<Map<String, Object>> list, Object alarmEndTm) {
        int q=0;
        try{
            Map<String, Object> insertMap = new HashMap<>();
            for (Map<String, Object> map : list) {
                insertMap.put("pumpEventTm", map.get("pumpEventTm"));
                insertMap.put("slaveId", map.get("slaveId"));
                insertMap.put("seq", map.get("seq"));
                insertMap.put("alarmEndTm", alarmEndTm);
                insertMap.put("rValue", map.get("rValue"));
                insertMap.put("sValue", map.get("sValue"));
                insertMap.put("tValue", map.get("tValue"));
                insertMap.put("lValue", map.get("lValue"));
                sqlSessionTemplate.insert("Modbus.InsertSlave031VO", insertMap);
            }
            q=1;
        }catch(Exception e ){
            log.warn("Slave031InsertError : "+e);
        }
        return q;
    }

    public int insertSlave031(Map<String, Object> map) {
        return sqlSessionTemplate.insert("Modbus.InsertSlave031VO", map);
    }












    public int updateAlarm(Map<String, Object> map){
        return sqlSessionTemplate.update("Modbus.updateAlarm", map);
    }

    public List<Map<String, Object>> saveData(Map<String, Object> map){
        return sqlSessionTemplate.selectList("Modbus.saveData", map);
    }



}
