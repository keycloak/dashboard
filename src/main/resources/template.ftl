<#macro page title="">
<!DOCTYPE html>
<html>
<head>
    <title><#if title?has_content>${title} - </#if>Keycloak Dashboard</title>
    <link rel="stylesheet" href="styles.css">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv='cache-control' content='no-cache'>
    <meta http-equiv='expires' content='0'>
    <meta http-equiv='pragma' content='no-cache'>
</head>
<body>

<ul class="menu">
    <li><a href="<#if publish>./<#else>index.html</#if>">Overview</a></li>
    <li><a href="workflows<#if !publish>.html</#if>">Workflows</a></li>
    <li><a href="prs<#if !publish>.html</#if>">PRs</a></li>
    <li><a href="bugs<#if !publish>.html</#if>">Bugs</a></li>
    <li><a href="tests<#if !publish>.html</#if>">Tests</a></li>
    <li><a href="config<#if !publish>.html</#if>">Config</a></li>
</ul>
<div class="updated">Last updated: ${updatedDate?string["dd MMMM yyyy, HH:mm '('zzz')'"]}</div>

<#nested>

<div class="clear" style="padding: 5px"></div>:

</body>
</html>
</#macro>