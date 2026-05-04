<#if missingLabelsEnhancements?has_content>
<div class="header">
    Enhancements missing labels (top ${missingLabelsEnhancements?size} by reactions)
</div>
<div class="body">
    <table>
        <tr>
            <th>Issue</th>
            <th class="center">Reactions</th>
            <th class="center">Comments</th>
            <th>Created</th>
        </tr>
        <#list missingLabelsEnhancements as issue>
        <tr>
            <td class="title"><a href="https://github.com/keycloak/keycloak/issues/${issue.number?c}">#${issue.number?c} ${issue.title}</a></td>
            <td class="count center">${issue.reactionsCount}</td>
            <td class="count center">${issue.commentsCount}</td>
            <td>${issue.createdAt?string["dd MMM yyyy"]}</td>
        </tr>
        </#list>
    </table>
</div>
</#if>
