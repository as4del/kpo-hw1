package ru.hse.ledger;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.hse.ledger.inspection.SafetyInspection;
import ru.hse.ledger.inspection.ServiceCenter;
import ru.hse.ledger.registry.AdmissionDesk;
import ru.hse.ledger.registry.AssetRegister;
import ru.hse.ledger.report.FleetReports;

@Configuration
public class Bootstrap {
    @Bean
    public AssetRegister register() {
        return new AssetRegister();
    }

    @Bean
    public ServiceCenter serviceCenter() {
        return new SafetyInspection();
    }

    @Bean
    public AdmissionDesk admissions(AssetRegister register, ServiceCenter serviceCenter) {
        return new AdmissionDesk(register, serviceCenter);
    }

    @Bean
    public FleetReports reports(AssetRegister register) {
        return new FleetReports(register);
    }
}
