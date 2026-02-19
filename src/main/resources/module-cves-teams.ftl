<div class="header">
    CVEs per team
</div>
<div class="body">
    <table>
        <tr>
            <th>Team</th>
            <#list cveTeamStats[0].columns as col>
            <th class="center">${col.label}</th>
            </#list>
        </tr>
        <#list cveTeamStats as cveTeamStat>
        <tr>
            <td><a href="${cveTeamStat.teamGhLink}">${cveTeamStat.title}</a></td>
            <#list cveTeamStat.columns as col>
            <td class="count ${col.cssClasses} center"><a href="${col.ghLink}">${col.count}</a></td>
            </#list>
        </tr>
        </#list>
    </table>
</div>
