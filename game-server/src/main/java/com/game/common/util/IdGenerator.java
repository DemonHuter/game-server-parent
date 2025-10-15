package com.game.common.util;


import com.game.model.player.login.PlayerLoginHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {
	private static final Logger logger = LoggerFactory.getLogger(IdGenerator.class);
	private static IdGenerator INSTANCE = new IdGenerator();
	private long machineId;

	private AtomicLong lastId;

	private long twepoch = 1519833600000L;   // 基准时间 2018-03-01 00:00:00

	private long machineIdBits = 13L;
	private long maxMachineId = -1L ^ (-1L << machineIdBits);
	
	private long sequenceBits = 12L;

	private long machineIdShift = sequenceBits;
	private long timestampLeftShift = sequenceBits + machineIdBits;
	private long maxSequence = (-1L ^ (-1L << sequenceBits)) - 1L;// 序列号的最大值
	private long sequenceMask = (-1L ^ (-1L << sequenceBits));
    
	private IdGenerator() {
		
	}
	public static IdGenerator getInstance() {
		return INSTANCE;
	}
	
	public void init(long machineId) {
		// sanity check for workerId
		if (machineId > maxMachineId || machineId < 0) {
			throw new IllegalArgumentException(
					String.format("machine Id can't be greater than %d or less than 0", maxMachineId));
		}
		logger.info("ID generator starting. timestamp left shift " + timestampLeftShift + ", machine id bits "
				+ machineIdBits + ", sequence bits " + sequenceBits + ", machineId " + machineId);
		this.machineId = machineId;
		lastId = new AtomicLong((timeGen() - twepoch) << timestampLeftShift | randStartSequence());
	}
	
    private long randStartSequence() {
    	return ThreadLocalRandom.current().nextInt(10);// new Random().nextInt(10);
    }

	private IdGenerator(long machineId) {
		init(machineId);
	}
    
    /**
     * 最高位固定为0
     * +------------------------+---------------------------+-----------------+
     * | 38bits timestamp in ms | 13bits worker(machine) ID | 12bits sequence |
     * +------------------------+---------------------------+-----------------+
     * @return
     */
	public synchronized long generateId() {
		while (true) {
			long now = timeGen();
			long oldId = lastId.get();
			long lastTimeInterval = oldId >>> timestampLeftShift;
			long nowInterval = now - twepoch;
			long sequence;
			if (nowInterval < lastTimeInterval) {
				Thread.yield();
				continue;
			} else if (nowInterval == lastTimeInterval) {
				sequence = oldId & sequenceMask;
				if (sequence >= maxSequence) {
					now = tillNextMillis(now);
					nowInterval = now - twepoch;
					sequence = randStartSequence();
				} else {
					sequence = sequence + 1;
				}
			} else {
				sequence = randStartSequence();
			}
			long newId = (nowInterval << timestampLeftShift) | (machineId << machineIdShift) | sequence;
			if (!lastId.compareAndSet(oldId, newId)) {
				Thread.yield();
				continue;
			}
			return newId;
		}
	}
	
	private long tillNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			Thread.yield();
			timestamp = timeGen();
		}
		return timestamp;
	}

    private long timeGen(){
        return System.currentTimeMillis();
    }
    
}

