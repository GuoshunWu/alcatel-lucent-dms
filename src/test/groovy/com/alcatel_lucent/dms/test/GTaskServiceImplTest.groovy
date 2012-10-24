package com.alcatel_lucent.dms.test

import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.TransactionConfiguration
import com.alcatel_lucent.dms.service.TaskService
import org.springframework.beans.factory.annotation.Autowired
import org.junit.Test
import org.junit.BeforeClass
import com.alcatel_lucent.dms.model.Text

import static org.junit.Assert.*
import com.alcatel_lucent.dms.service.DaoService
import net.sf.json.JSONObject
import com.alcatel_lucent.dms.BusinessException

import com.alcatel_lucent.dms.service.DaoService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
class GTaskServiceImplTest {
	
	@BeforeClass
	static void setUpBeforeClass() throws Exception {

	}

	@Autowired
	private DaoService dao;
	
	@Autowired
	private TaskService taskService;
	
	@Test
	void testCreateTask() {
		List<Long> dictIds = [322l, 323l]
		List<Long> languageIds = [6l, 8l]
		taskService.createTask(1, "Test task", dictIds, languageIds)
	}
	
	@Test
	void testCancelTask() {
		taskService.cancelTask(3);
	}

}
