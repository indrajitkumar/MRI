package com.brainbox.log;

import com.brainbox.log.vo.AppLogData;
import com.brainbox.mobile.exception.SystemException;

public interface LogSender {
	void send(AppLogData report) throws SystemException;
}
