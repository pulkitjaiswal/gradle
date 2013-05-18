/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.performance.fixture

import org.gradle.integtests.fixtures.executer.GradleDistribution
import org.gradle.performance.ResultSpecification
import org.gradle.performance.measure.DataAmount
import org.gradle.performance.measure.Duration

class PerformanceTestRunnerTest extends ResultSpecification {
    final timer = Mock(OperationTimer)
    final testProjectLocator = Stub(TestProjectLocator)
    final dataCollector = Stub(DataCollector)
    final currentGradle = Stub(GradleDistribution)
    final runner = new PerformanceTestRunner(timer: timer, testProjectLocator: testProjectLocator, dataCollector: dataCollector, current: currentGradle)

    def "runs test and builds results"() {
        given:
        runner.testProject = 'test1'
        runner.targetVersions = ['1.0', '1.1']
        runner.tasksToRun = ['build']
        runner.warmUpRuns = 1
        runner.runs = 4

        when:
        def results = runner.run()
        results.current.size() == 4
        results.current.avgTime() == Duration.seconds(10)
        results.current.avgMemory() == DataAmount.kbytes(10)
        results.baselineVersions*.version == ['1.0', '1.1']
        results.baselineVersions[0].results.size() == 4
        results.baselineVersions[1].results.size() == 4

        then:
        // warmup runs are discarded
        3 * timer.measure(_) >> operation(executionTime: Duration.seconds(100), heapUsed: DataAmount.kbytes(100))
        12 * timer.measure(_) >> operation(executionTime: Duration.seconds(10), heapUsed: DataAmount.kbytes(10))
        0 * timer._
    }
}