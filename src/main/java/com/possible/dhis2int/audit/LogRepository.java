package com.possible.dhis2int.audit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class LogRepository {
static LogRepository	logRepository;
public static Recordlog save(Recordlog dhis2_report_status) {

    logRepository.save(dhis2_report_status);

   
    return dhis2_report_status;
}
public static List<Recordlog> findAll() {
	// TODO Auto-generated method stub
	return null;
}

}
