<#if weaknessTeamStats?has_content>
<div class="header">
    Weaknesses per team
</div>
<div class="body">
    <table>
        <tr>
            <th>Team</th>
            <#list weaknessTeamStats[0].columns as col>
            <th class="center">${col.label}</th>
            </#list>
        </tr>
        <#list weaknessTeamStats as weaknessTeamStat>
        <tr>
            <td><a href="${weaknessTeamStat.teamGhLink}">${weaknessTeamStat.title}</a></td>
            <#list weaknessTeamStat.columns as col>
            <td class="count ${col.cssClasses} center"><a href="${col.ghLink}">${col.count}</a></td>
            </#list>
        </tr>
        </#list>
    </table>
</div>
</#if>
