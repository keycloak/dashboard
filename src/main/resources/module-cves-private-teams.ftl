<#if !publish && privateCveTeamStats?has_content>
<div class="header">
    CVEs per team - keycloak-private
</div>
<div class="body">
    <table>
        <tr>
            <th>Team</th>
            <#list privateCveTeamStats[0].columns as col>
            <th class="center">${col.label}</th>
            </#list>
        </tr>
        <#list privateCveTeamStats as stat>
        <tr>
            <td><a href="${stat.teamGhLink}">${stat.title}</a></td>
            <#list stat.columns as col>
            <td class="count ${col.cssClasses} center"><a href="${col.ghLink}">${col.count}</a></td>
            </#list>
        </tr>
        </#list>
    </table>
</div>
</#if>
