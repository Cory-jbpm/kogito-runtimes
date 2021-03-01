/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.quarkus.deployment.devconsole;

import java.io.IOException;
import java.nio.file.Path;

import org.kie.kogito.quarkus.runtime.KogitoSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.bootstrap.model.AppArtifact;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.pkg.builditem.CurateOutcomeBuildItem;
import io.quarkus.deployment.util.WebJarUtil;
import io.quarkus.devconsole.spi.DevConsoleRuntimeTemplateInfoBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.runtime.devmode.DevConsoleRecorder;

public class DevConsoleProcessor {

    private static String STATIC_RESOURCES_PATH = "dev-static/";
    private static final Logger LOGGER = LoggerFactory.getLogger(DevConsoleProcessor.class);

    @BuildStep(onlyIf = IsDevelopment.class)
    public DevConsoleRuntimeTemplateInfoBuildItem collectBeanInfo() {
        LOGGER.warn("deployStaticResources");
        return new DevConsoleRuntimeTemplateInfoBuildItem("info",
                new KogitoSupplier());
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    @Record(ExecutionTime.RUNTIME_INIT)
    public void deployStaticResources(DevConsoleRecorder recorder, CurateOutcomeBuildItem curateOutcomeBuildItem,
            LaunchModeBuildItem launchMode, ShutdownContextBuildItem shutdownContext,
            BuildProducer<RouteBuildItem> routeBuildItemBuildProducer) throws IOException {
        LOGGER.warn("deployStaticResources");
        AppArtifact devConsoleResourcesArtifact = WebJarUtil.getAppArtifact(curateOutcomeBuildItem, "org.kie.kogito", "kogito-quarkus-deployment");

        Path devConsoleStaticResourcesDeploymentPath = WebJarUtil.copyResourcesForDevOrTest(curateOutcomeBuildItem, launchMode,
                devConsoleResourcesArtifact, STATIC_RESOURCES_PATH);

        LOGGER.warn("path=== " + devConsoleStaticResourcesDeploymentPath.toString());
        routeBuildItemBuildProducer.produce(new RouteBuildItem.Builder()
                .route("/kogito/dev/resources/*")
                .handler(recorder.devConsoleHandler(devConsoleStaticResourcesDeploymentPath.toString(), shutdownContext))
                .nonApplicationRoute(false)
                .build());
    }
}