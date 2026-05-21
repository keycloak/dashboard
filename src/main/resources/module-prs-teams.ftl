<div class="header">
    Pull Requests per team
</div>
<div class="body">
    <table>
        <tr>
            <th>Description</th>
            <th class="center">Open</th>
        </tr>
        <#list prTeamStats as prStat>
        <tr>
            <td class="title"><a href="${prStat.openGhLink}">${prStat.title}</a></td>
            <td class="count ${prStat.openCssClasses} center"><a href="${prStat.openGhLink}">${prStat.openCount}</a></td>
        </tr>
        </#list>
    </table>
</div>