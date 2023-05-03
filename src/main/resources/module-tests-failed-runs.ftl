<a id="failed-runs"></a>
<div class="header">
    Failed runs
</div>
<div class="body">
    <ul class="body-menu">
        <#list failedRuns as run>
        <li> <a href="#failed-run-${run.runId}" class="nowrap">${run.date?date} - ${run.runId}</a></li>
        </#list>
    </ul>

    <table>
        <#list failedRuns as run>
        <tr>
            <th colspan="3">
                <a id="failed-run-${run.runId}"></a>
                <a href="https://github.com/keycloak/keycloak/actions/runs/${run.runId}">${run.date?date} - ${run.runId}</a>
            </th>
        </tr>
        <#list run.failedJobs as job>
        <tr>
            <td class="size20">${job.name}</td>
            <td class="size10">${job.conclusion}</td>
            <#if job.errorLog?has_content>
            <td class="failures">
                <div class="failures">
                    <#list job.errorLog as log>
                    ${log}&nbsp;<br/><br/>
                </#list>
                ${job.failedGoal!}
                </div>
            </td>
            <#else>
            <td></td>
            </#if>
        </tr>
        </#list>
    </#list>
    </table>
</div>
