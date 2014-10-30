angular.module('angular.atmosphere.chat', [ 'ngGrid', 'angular.atmosphere','ui.bootstrap' ]);

function ChatController($scope, $http, $log, atmosphereService) {
	$scope.wip = false;

	// Log Grid
	$scope.logItems = [];
	$scope.logGridOptions = {
		data : 'logItems',
		enableColumnResize : true,
		columnDefs : [ {
			field : 'index',
			displayName : '',
			width : "157px",
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'tstamp',
			displayName : '',
			width : "157px",
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'line',
			displayName : 'Line',
			cellClass : "leftAlign ngSmallFont"
		} ]

	};
	function focusRow(rowToSelect,gridOpts) {
		  gridOpts.selectItem(rowToSelect, true);
		  var grid = gridOpts.ngGrid;
		  grid.$viewport.scrollTop(grid.rowMap[rowToSelect] * grid.config.rowHeight);
		};
		
	// Metrics grid
	var w_ = "110px"; 
	$scope.metricsItems = [];
	$scope.metricsGridOptions = {
		data : 'metricsItems',
		enableColumnResize : true,
		columnDefs : [ {
			field : 'index',
			displayName : '#',
			width : "30px",
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'Stepname',
			displayName : 'Stepname',
			width : w_,
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'Copynr',
			displayName : 'Copynr',
			width : w_,
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'Read',
			displayName : 'Read',
			width : w_,
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'Written',
			displayName : 'Written',
			width : w_,
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'Input	',
			displayName : 'Input',
			width : w_,
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'Output',
			displayName : 'Output',
			width : w_,
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'Updated',
			displayName : 'Updated',
			width : w_,
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'Rejected',
			displayName : 'Rejected',
			width : w_,
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'Errors',
			displayName : 'Errors',
			width : w_,
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'Active',
			displayName : 'Active',
			width : w_,
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'Time',
			displayName : 'Time',
			width : w_,
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'Speed',
			displayName : 'Speed(r/s)',
			width : w_,
			cellClass : "leftAlign ngSmallFont"
		},{
			field : 'inputoutput',
			displayName : 'Input/Output',
			cellClass : "leftAlign ngSmallFont"
		} ]

	};

	$scope.model = {
		transport : 'websocket',
		messages : []
	};

	var socket;

	var request = {
		url : 'http://' + window.location.host.toString()
				+ '/ged/service/tenant1000',
		contentType : 'application/json',
		logLevel : 'debug',
		transport : 'websocket',
		trackMessageLength : true,
		reconnectInterval : 5000,
		enableXDR : true,
		timeout : 60000
	};

	request.onOpen = function(response) {
		$scope.model.transport = response.transport;
		$scope.model.connected = true;
		$scope.model.content = 'Connected using ' + response.transport;
	};

	request.onClientTimeout = function(response) {
		$scope.model.content = 'Client closed the connection after a timeout. Reconnecting in '
				+ request.reconnectInterval;
		$scope.model.connected = false;
		socket
				.push(atmosphere.util
						.stringifyJSON({
							author : author,
							message : 'is inactive and closed the connection. Will reconnect in '
									+ request.reconnectInterval
						}));
		setTimeout(function() {
			socket = atmosphereService.subscribe(request);
		}, request.reconnectInterval);
	};

	request.onReopen = function(response) {
		$scope.model.connected = true;
		$scope.model.content = 'Re-connected using ' + response.transport;
	};

	// For demonstration of how you can customize the fallbackTransport using
	// the onTransportFailure function
	request.onTransportFailure = function(errorMsg, request) {
		atmosphere.util.info(errorMsg);
		request.fallbackTransport = 'long-polling';
		$scope.model.header = 'Default transport is WebSocket, fallback is '
				+ request.fallbackTransport;
	};

	request.onMessage = function(response) {
		var responseText = response.responseBody;
		try {
			var message = atmosphere.util.parseJSON(responseText);
			if ('REQUEST_UPDATE' === message.responseType) {//Route to live-grid's
				if ('TRANS_GRID' === message.msgUpdateType) {
					if ($scope.metricsItems.length == 0) {
						for ( var i=0; i < message.stepLines.length; i++) {
							var step = message.stepLines[i];
							step.index = (i+1);
							step.inputoutput = '-';
							$scope.metricsItems[i] = step;
						}
					}
					else {
						var temp_ = angular.copy($scope.metricsItems);
						for ( var i=0; i < message.stepLines.length; i++) {
							var step = message.stepLines[i];
							step.index = (i+1);
							step.inputoutput = '-';
							temp_.splice(i,1,step);
						}
						$scope.metricsItems = temp_;
					}
				} else if ('TRANS_LOG' === message.msgUpdateType) {
					if (!$scope.logItems[message.maxIndex]) {
						for (var i = 0; i <= message.maxIndex; i++) {//init empty's
							if (!$scope.logItems[i]) {
								$scope.logItems[i] = {};
							}
						}
					}
					focusRow(message.logLines[0][0]-1,$scope.logGridOptions);
					for ( var i=0; i < message.logLines.length; i++) {
						var line = message.logLines[i];
						var iIndex = line[0];
						var eIndex = line[1];
						var tIndex = line[2];
						var lIndex = line[3];
						$scope.logItems[iIndex] = {
							index : iIndex,
							tstamp: tIndex,
							line : lIndex
						};
					}
				}
			} else if ('RUN_STARTED' === message.responseType) {
				$scope.wip = true;
				$log.info('Trans(' + $scope.selectedItem.carteObjectEntry.id
						+ ') started');
			} else if ('RUN_FINISHED' === message.responseType) {
				$scope.wip = false;
				$log.info('Trans(' + $scope.selectedItem.carteObjectEntry.id
						+ ') finished');
			}
		} catch (e) {
			$log.error("Error parsing JSON: ", responseText);
			throw e;
		}
	};

	request.onClose = function(response) {
		$scope.model.connected = false;
		$scope.model.content = 'Server closed the connection after a timeout';
		socket.push(atmosphere.util.stringifyJSON({
			author : $scope.model.name,
			message : 'disconnecting'
		}));
	};

	request.onError = function(response) {
		$scope.model.content = "Sorry, but there's some problem with your socket or the server is down";
		$scope.model.logged = false;
	};

	request.onReconnect = function(request, response) {
		$scope.model.content = 'Connection lost. Trying to reconnect '
				+ request.reconnectInterval;
		$scope.model.connected = false;
	};

	socket = atmosphereService.subscribe(request);

	// Trans list
	var listTransUrl = "/api/carte/transformations";
	$http.get(listTransUrl).success(function(data) {
		if (angular.isArray(data)) {

		} else {
			$scope.transItems = [ data ]
		}
	});

	$scope.submit = function() {
		if ($scope.selectedItem) {
			$scope.logItems = [];
			socket
					.push(atmosphere.util
							.stringifyJSON({
								type : 'EXEC_TRANS',
								params : {
									PARAM_CARTE_OBJECT_ID : $scope.selectedItem.carteObjectEntry.id,
									PARAM_META_NAME : $scope.selectedItem.carteObjectEntry.name
								}
							}));
		}
	};

	var input = $('#input');
	input.keydown(function(event) {
		var me = this;
		var msg = $(me).val();
		if (msg && msg.length > 0 && event.keyCode === 13) {
			$scope.$apply(function() {
				// First message is always the author's name
				if (!$scope.model.name)
					$scope.model.name = msg;

				socket.push(atmosphere.util.stringifyJSON({
					author : $scope.model.name,
					message : msg
				}));
				$(me).val('');
			});
		}
	});
}
