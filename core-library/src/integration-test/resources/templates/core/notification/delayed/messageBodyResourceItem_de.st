<div id="resource">
Resource : $resource.name$<br/>
	<div id="resource_body" style="margin-left:15px;">
		$if(resource.description)$- description : $resource.description$<br/>$endif$
		- location : $resource.location$<br/>
		$if(resource.url)$- link : <a href="$resource.url$" target=_blank>Click here ...</a><br/>$endif$
		- notification$if(resource.severalNotifications)$s$endif$ :
		<div id="resource_item" style="margin-left:25px;">
			$first(resource.notifications):{ n | $messageBodyResourceNotificationItem_de(notification=n)$}$$rest(resource.notifications):{ n | $if(!n.previousHasMessage)$<br/>$endif$$messageBodyResourceNotificationItem_de(notification=n)$}$
		</div>
	</div>
</div>