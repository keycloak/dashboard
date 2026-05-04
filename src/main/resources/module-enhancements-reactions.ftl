<#if topReactedEnhancements?has_content>
<div class="header">
    Most reacted enhancement requests
</div>
<div class="body">
    <table>
        <tr>
            <th>Issue</th>
            <th class="center">Reactions</th>
            <th>Area</th>
            <th>Team</th>
        </tr>
        <#list topReactedEnhancements as issue>
        <tr>
            <td class="title"><a href="https://github.com/keycloak/keycloak/issues/${issue.number?c}">#${issue.number?c} ${issue.title}</a></td>
            <td class="count center">${issue.reactionsCount}</td>
            <td><#list issue.areas as area>${area?replace("area/", "")}<#sep>, </#list></td>
            <td><#list issue.teams as team>${team?replace("team/", "")}<#sep>, </#list></td>
        </tr>
        </#list>
    </table>
</div>
</#if>
