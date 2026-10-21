<a id="retried-green-jobs"></a>
<div class="header">
    Errors that failed a job and passed on re-run (likely flaky)
</div>
<div class="body">
    <table>
        <tr>
            <th>Error</th>
            <th class="center">Count</th>
            <th>Jobs</th>
            <th>Last seen</th>
            <th>Sample failed attempts</th>
        </tr>
        <#list flakyErrors as flakyError>
        <tr>
            <td title="${flakyError.error?html}">${flakyError.error?truncate(100)}</td>
            <td class="count <#if flakyError.count gte 5>error<#elseif flakyError.count gte 2>warn<#else>blank</#if> center">${flakyError.count}</td>
            <td><#list flakyError.jobs as job>${job}<#sep>, </#sep></#list></td>
            <td>${flakyError.lastSeen?date}</td>
            <td>
                <#list flakyError.samples?chunk(3) as line>
                <#list line as job>
                <a title="${job.name?html}<#if job.failedStep?has_content> / ${job.failedStep?html}</#if>" href="https://github.com/keycloak/keycloak/actions/runs/${job.failedRun.runId}<#if job.failedRun.attempt?has_content>/attempts/${job.failedRun.attempt}</#if>">${job.failedRun.runId}</a><#sep>, </#sep>
                </#list><#sep><br/></#sep>
                </#list>
            </td>
        </tr>
        </#list>
    </table>
</div>
