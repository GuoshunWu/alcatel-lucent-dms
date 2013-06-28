package com.alcatel_lucent.dms.test

import com.alcatel_lucent.dms.service.DaoService
import com.alcatel_lucent.dms.service.TaskService
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.transaction.annotation.Transactional

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@Transactional
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
		taskService.createTask(1, null, "Test task", dictIds, languageIds)
	}
	
	@Test
	void testCloseTask() {
		taskService.closeTask(4);
	}
	
	@Test
	void testGenerateTaskFiles() {
		String dir = "d:/temp/task_test"
		taskService.generateTaskFiles(dir, 12);
	}
	
	@Test
	void testReceiveTaskFiles() {
		String dir = "d:/temp/task_test"
		taskService.receiveTaskFiles(2, dir);
	}
	
	@Test
	void testApplyTask() {
		taskService.applyTask(2, true);
	}

}
