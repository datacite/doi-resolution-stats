var datacite = (function(){
	var self = {};
	self.con = null;
	self.period = "monthly";
	self.doi = "";
	self.title = "All Datacite resolutions";
	self.data = null;

	self.start = function(){
		self.initSpinner();
		self.fetchResults();
		self.fetchHits();
		self.initUI();
	};
	
	//Create Ajax Spinner event handlers
	self.initSpinner = function(){
		$(document).on({
			ajaxStart : function() {
				console.log("ajax start");
				$("body").addClass("ajaxloading");
			},
			ajaxStop : function() {
				console.log("ajax stop");
				$("body").removeClass("ajaxloading");
			}
		});
	};
	
	//Setup button click handlers etc
	self.initUI = function(){
		$("#monthly-btn").click(function() {
			self.period = 'monthly';
			self.fetchResults();
		});
		$("#daily-btn").click(function() {
			self.period = 'daily';
			self.fetchResults();
		});

		// publisher typeahead
		$('<input></input>').attr("id", "datacentres").addClass(
				'form-control').addClass('typeahead').attr('placeholder',
				'Choose datacentre:').appendTo($('.datacentres'));
		$('#datacentres').typeahead([ {
			name : 'datacentres',
			header : '<h4>Datacentres</h4>',
			prefetch : 'api/dois/prefixes'
		}, {
			name : 'alldatacite',
			header : '<h4>All Datacite resolutions</h4>',
			local : [ {
				"value" : "All Datacite Resolutions",
				"doi" : ""
			} ]
		}

		]).on('typeahead:selected', function(obj, datum) {
			self.doi = datum.doi;
			self.title = datum.value;
			self.fetchResults();
			self.fetchHits();
		});

		// toggle
		$('.btn-toggle').click(function() {
			$(this).find('.btn').toggleClass('active');

			if ($(this).find('.btn-primary').size() > 0) {
				$(this).find('.btn').toggleClass('btn-primary');
			}
			if ($(this).find('.btn-danger').size() > 0) {
				$(this).find('.btn').toggleClass('btn-danger');
			}
			if ($(this).find('.btn-success').size() > 0) {
				$(this).find('.btn').toggleClass('btn-success');
			}
			if ($(this).find('.btn-info').size() > 0) {
				$(this).find('.btn').toggleClass('btn-info');
			}

			$(this).find('.btn').toggleClass('btn-default');

		});
	};
	
	self.createSlider = function(el){
		//data-slider-min="10" data-slider-max="1000" data-slider-step="5" data-slider-value="[250,450]"
		$(el).slider("destroy");
		$(el).attr("data-slider-min",0);
		$(el).attr("data-slider-max",self.data.length);
		$(el).attr("data-slider-step",1);
		$(el).attr("data-slider-value","[0,"+(self.data.length-1)+"]");
		
		var formatter = function(value) {
			if (!value[1])
				return "";
			var from  = moment(self.data[value[0]].x).format('Do MMM YY');
			var to  = moment(self.data[value[1]].x).format('Do MMM YY');
			return from +" - "+to;
		};
		$(el).slider({formatter:formatter});
		
		$(el).on("slide", function(slideEvt) {
			self.con.setData(self.data.slice(slideEvt.value[0], slideEvt.value[1])).render();
		});
		$(el).show();
	};

	//Query the REST appi for stats
	self.fetchResults = function() {
		var url = "";
		if (self.doi)
			url = 'api/stats/' + self.period + '/' + self.doi + '?map';
		else
			url = 'api/stats/' + self.period + '?map';

		$
				.ajax({
					url : url,
					type : 'GET',
					contentType : 'application/json; charset=utf-8',
					dataType : 'json',
					async : false,
					success : function(result) {
						$('#linecharttitle')
								.html(
										self.title
												+ " ("
												+ self.period
												+ ")"
												+ ' <small><a href="#">csv</a> <a href="#">json</a> <a href="#">xml</a></small>');
						$("#linechart").empty();
						
						self.data = [];

						for ( var date in result) {
							var d = new Date(date);
							var o = {};
							o.x = d;
							o.y = result[date];
							self.data.push(o);

						}
						console.log(self.data);
						
						self.createContour("#linechart", self.data);
						self.createSlider("#dateslider");
					},
					error : function(jq, textStatus, errorThrown) {
					}
				});
	};

	//Query the REST api for top 100 DOIs
	self.fetchHits = function() {
		var url = "";
		if (self.doi)
			url = 'api/stats/hits/' + self.doi + "?limit=100";
		else
			url = 'api/stats/hits?limit=100';
		$.ajax({
			url : url,
			type : 'GET',
			contentType : 'application/json; charset=utf-8',
			dataType : 'json',
			async : false,
			success : function(result) {
				$("#hitstable").empty();
				self.populateTable("#hitstable", result);
			},
			error : function(jq, textStatus, errorThrown) {
			}
		});
	};

	self.populateTable = function(elem, data) {
		for ( var obj in data) {
			$('#hitstable').append(
					'<tr><td><a href="http://dx.doi.org/' + data[obj].doi + '">'
							+ data[obj].doi + '</a></td><td style="width:33%">'
							+ data[obj].count + '</td></tr>');
		}
	};

	self.createContour = function(elem, renderdata) {
		//var largest = 0;
		for (vals in renderdata){
			
		}
		self.con = new Contour({
			el : elem,
			chart : {
				height : 300,
				gridlines : 'horizontal',
				padding : {
					right : 40
				}/*,
				animations : {
					duration : 1000
				}*/
			},
			xAxis : {
				maxTicks : 10,
				/*maxTicks : 10,
				linearDomain : true,
				type : 'time',*/
				labels : {
					formatter : function(d) {
						return moment(d).format('MMM YY');
					}
				}
			},
			yAxis : {
				title : 'Hits'
					/*,
				max : largest*/
			},
			line : {
				marker : {
					enable : false
				}

			},
			tooltip : {
				formatter : function(d) {
					return moment(d.x).format('Do MMM YY') + ' Hits: ' + d.y;
				}
			}
		}).cartesian().line(renderdata)/*.column(renderdata)*/.tooltip().render();
	};
	return self;
}());

$(document).ready(
		function() {
			datacite.start();
		});

