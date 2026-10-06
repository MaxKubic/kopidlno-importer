package cz.smartform.importer;

import cz.smartform.importer.service.RuianImportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KopidlnoImporterApplication implements CommandLineRunner {

    private final RuianImportService importService;

    public KopidlnoImporterApplication(RuianImportService importService) {
        this.importService = importService;
    }

    public static void main(String[] args) {
        SpringApplication.run(KopidlnoImporterApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        importService.importData();
    }
}