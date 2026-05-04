<div class="header">
    Enhancements per team
</div>
<div class="body">
    <table>
        <tr>
            <th>Team</th>
            <#list enhancementTeamStats[0].columns as col>
            <th class="center">${col.label}</th>
            </#list>
        </tr>
        <#list enhancementTeamStats as teamStat>
        <tr>
            <td><a href="${teamStat.teamGhLink}">${teamStat.title}</a></td>
            <#list teamStat.columns as col>
            <td class="count ${col.cssClasses} center"><a href="${col.ghLink}">${col.count}</a></td>
            </#list>
        </tr>
        </#list>
    </table>
</div>
