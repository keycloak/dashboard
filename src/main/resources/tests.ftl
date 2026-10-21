<#import "template.ftl" as template>
<@template.page title="Tests">

<ul class="sub-menu">
    <li><a href="#retried-green-jobs">Failed, then green on re-run</a></li>
    <li><a href="#unlinked-failed-jobs">Recent failed jobs</a></li>
    <li><a href="#linked-failed-jobs">Reported Failed jobs</a></li>
    <li><a href="#flaky-tests">Flaky tests</a></li>
    <li><a href="#resolved-runs">Resolved failed jobs</a></li>
</ul>

<div class="content">
    <div class="modal-float clear">
        <div class="modal-float-margin">
            <#include "module-tests-retried-green-jobs.ftl">
        </div>
    </div>
    <div class="modal-float clear">
        <div class="modal-float-margin">
            <#include "module-tests-unlinked-failed-jobs.ftl"></div>
        </div>
    </div>
    <div/>
    <div class="modal-float clear">
        <div class="modal-float-margin">
            <#include "module-tests-linked-failed-jobs.ftl">
        </div>
    </div>
    <div class="modal-float clear">
        <div class="modal-float-margin">
            <#include "module-tests-flaky-tests.ftl">
        </div>
    </div>
    <div/>
    <div class="modal-float clear">
        <div class="modal-float-margin">
            <#include "module-tests-resolved-runs.ftl">
        </div>
    </div>
</div>

</@template.page>