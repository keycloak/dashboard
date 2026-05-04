<#import "template.ftl" as template>

<@template.page title="Enhancements">
<div class="content">
    <div class="modal-float">
        <div class="modal-float-margin"><#include "module-enhancements.ftl"></div>
    </div>
    <div class="modal-float">
        <div class="modal-float-margin"><#include "module-enhancements-teams.ftl"></div>
    </div>
    <div class="modal-float">
        <div class="modal-float-margin"><#include "module-enhancements-reactions.ftl"></div>
    </div>
</div>
</@template.page>