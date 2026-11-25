package com.meridian.lending.service.accrual;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Accrual on the mainframe: asks for JCL job NIGHTLY to run, whose STEP010 runs ACCRUE01.
 * The batch scheduler watches the trigger folder and submits NIGHTLY when a trigger file appears.
 */
@Component
public class CobolAccrualBridge implements AccrualEngine {
    private final Path triggerDir;

    public CobolAccrualBridge(@Value("${lending.accrual.trigger-dir:/data/batch/trigger}") String triggerDir) {
        this.triggerDir = Path.of(triggerDir);
    }

    @Override
    public void accrue(LocalDate businessDate) {
        try {
            Files.writeString(triggerDir.resolve("NIGHTLY." + businessDate), "BUSDATE=" + businessDate + "\n");
        } catch (IOException e) {
            throw new UncheckedIOException("could not request job NIGHTLY", e);
        }
    }
}
