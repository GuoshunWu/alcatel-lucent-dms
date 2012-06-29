import com.alcatel_lucent.dms.SpringContext;
import com.alcatel_lucent.dms.service.DictionaryService;


public class TestSpringContext {
	
	public static void main(String[] args) throws Exception {
		SpringContext.getService(DictionaryService.class);
	}

}
