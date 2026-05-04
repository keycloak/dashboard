    <div class="header">
        Enhancements
    </div>
    <div class="body">
        <table>
            <tr>
                <th>Description</th>
                <th class="center">Open</th>
                <th class="center">Closed</th>
            </tr>
            <#list enhancementStats as stat>
            <tr>
                <td class="title"><a href="${stat.ghLink}">${stat.label}</a></td>
                <td class="count ${stat.cssClasses} center"><a href="${stat.ghLink}">${stat.count}</a></td>
                <#if stat.closedCount?has_content>
                <td class="count ${stat.closedCssClasses} center"><a href="${stat.closedGhLink}">${stat.closedCount}</a></td>
                <#else>
                <td class="count"></td>
                </#if>
            </tr>
            </#list>
        </table>
    </div>