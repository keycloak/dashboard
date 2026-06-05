<div class="header">
    CVEs in third party dependencies
</div>
<div class="body">
    <table>
        <tr>
            <th>Ref</th>
            <th>Criticial</th>
            <th>High</th>
            <th>Medium</th>
            <th>Low</th>
        </tr>
        <#list cveStats as cveStat>
        <tr>
            <td><a href="${cveStat.refLink}">${cveStat.ref}</a></td>
            <td class="count ${cveStat.criticalClass} center"><a href="${cveStat.criticialLink}">${cveStat.criticial}</a></td>
            <td class="count ${cveStat.highClass} center"><a href="${cveStat.highLink}">${cveStat.high}</a></td>
            <td class="count ${cveStat.mediumClass} center"><a href="${cveStat.mediumLink}">${cveStat.medium}</a></td>
            <td class="count ${cveStat.lowClass} center"><a href="${cveStat.lowLink}">${cveStat.low}</a></td>
        </tr>
        </#list>
    </table>
</div>
