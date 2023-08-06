package it.finanze.sanita.fse2.ms.srvquery;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.utility.FileUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@AutoConfigureMockMvc
public class GeneratorTest {

	@Test
	void generateRandomBundle() {
		byte[] bundleLDO = FileUtility.getFileFromInternalResources("Files" + File.separator + "BundleLDO.json");
		System.out.println(new String(bundleLDO));
		
	}
}
