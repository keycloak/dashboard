<#import "template.ftl" as template>

<@template.page title="Stars status">
<div class="content">
    <#list starsList as stars>
    <div class="modal-float">
        <div class="modal-float-margin">
            <div class="header">
                ${stars.year?c}
            </div>
            <div class="body">
                <table>
                    <tr>
                        <th>Month</th>
                        <th class="center">Added</th>
                        <th class="center">Target added</th>
                        <th class="center">Total</th>
                        <th class="center">Target total</th>
                    </tr>
                    <#list stars.months as m>
                    <tr>
                        <td class="title">${m.month}</td>
                        <td class="count ${m.addedStyles} center"><#if m.lapsed gt 0>${m.addedStars}</#if></td>
                        <td class="count center">${m.targetAddedStars}</td>
                        <td class="count ${m.countStyles} center"><#if m.lapsed gt 0>${m.totalStars}</#if></td>
                        <td class="count center">${m.targetTotalStars}</td>
                    </tr>
                    </#list>
                </table>
            </div>
        </div>
    </div>
    </#list>
</div>
</@template.page>