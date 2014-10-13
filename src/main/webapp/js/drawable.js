(function () {
    'use strict';

    var grab = false;
    var points;

    Contour.export('drawable', function (data, layer, options) {

        var w = options.chart.plotWidth;
        var h = options.chart.plotHeight;
        var _this = this;
        var prevPoint;

        points = data[0].data;

        layer.selectAll('.mouse-tracker').data([1])
        .enter()
        .append('rect').attr({
            'class': 'mouse-tracker',
            'width': w,
            'height': h,
            'x': 0,
            'y': 0,
            'fill': 'transparent'
        })
        .on('mousedown.draw', function () {
            grab = true;
            var refElement = this;
            prevPoint = d3.mouse(refElement);

            d3.select('body')
                .attr('style', 'cursor: pointer')
                .on('mouseup.draw', function () {
                    update.call(refElement, refElement, true);
                    grab = false;
                    d3.select(this)
                        .on('mousemove.draw', null).on('mouseup.draw', null)
                        .attr('style', 'cursor: default');
                })
                .on('mousemove.draw', function () {
                    if (!grab) return;
                    update.call(this, refElement, false);
                });
        });

        /*jshint eqnull:true*/
        function update(refLayer, animate) {
            animate = animate != null ? animate : false;
            var x = _this.xScale;
            var y = _this.yScale;
            if (!x.invert) {
                var d = x.domain();
                var r = x.range();
                var mouse = d3.mouse(refLayer);
                var xData = d[d3.bisect(r, mouse[0]) - 1];
                var prevX = d[d3.bisect(r, prevPoint[0]) - 1];
                var leftRight = prevX < xData;
                var lowX = leftRight ? prevX : xData;
                var highX = !leftRight ? prevX : xData;
                var upDown = prevPoint[1] < mouse[1];
                var lowY = upDown ? prevPoint[1] : mouse[1];
                var highY = !upDown ? prevPoint[1] : mouse[1];
                // generate an interpolator from the lowset Y to the highest Y
                var interpolator = !upDown ?
                    d3.interpolateNumber(highY, lowY) :
                    d3.interpolateNumber(lowY, highY);

                var curX = lowX;
                do {
                    // now calculate the t differently depending on
                    // whether we are moving left to right or right to left
                    var t = highX === lowX ? 1 : leftRight ? (curX - lowX) / (highX - lowX) : (curX - highX) / (lowX - highX);
                    var mouseY = interpolator(t);
                    var yData = y.invert(mouseY);
//prevent neg vals
if (yData < 0) yData=0;
yData = Math.round(yData);

                    points[d[curX]] = yData;
                } while(++curX < highX);

                // var yData = _.nw.clamp(y.invert(mouse[1]), yDomain[0], yDomain[1]);
                var prevAnimSetting = _this.options.chart.animations;
                _this.options.chart.animations = animate;
                
                _this.setData(points).render();
                
//callback here?
$(_this.options.el).trigger( {type:"contourUpdated", points:points} );
                
                _this.options.chart.animations = prevAnimSetting;

                prevPoint = mouse;
            }
        }
    });

    Contour.export('watermark', function (data, layer, options) {
        var w = options.chart.plotWidth;
        var bounds = _.nw.textBounds(data, '.watermark-text');
        var ds = _.isString(data) ? [null] : [];
        var text = layer.selectAll('.watermark-text')
            .data(ds);

        text.enter().append('text')
                .attr('class', 'watermark-text')
                .attr('x', (w - bounds.width) / 2 + options.chart.internalPadding.left)
                .attr('y', 100)
                .attr('opacity', 0.05)
                .text(data);

        text.text(data);

        text.exit()
            .transition().duration(1500)
            .attr('opacity', 0)
            .remove();
    });
    
    Contour.export('customLegend', function (data, layer, options) {
        var formatter = d3.format(',.2s');
        var barCenter = this.rangeBand / 2;
        var x = this.xScale, y = this.yScale;
        var duration = options.chart.animations.duration || 0;
        var em = _.nw.textBounds('123456789', '.label-text').height;

        // use d3 style enter/update/exit to render the data labels
        var labels = layer.selectAll('.label-text')
            .data(data[0].data);
        
        labels.enter().append('text')
            .attr('class', 'label-text');

        labels
            .attr('fill', function (d) {
                // var em = 10;
                var v = y(d.y) + em;
                var max = options.chart.plotHeight - em;
                var res = v <= max ? '#eee' : '#888';

                return res;
            })
            .attr('x', function (d) { return x(d.x) + barCenter; })
            .attr('text-anchor', 'middle')
            .text(function (d) { return formatter(d.y); })
            		//camp.dist[d.x])+"%";})
            .transition().duration(duration)
            .attr('y', function (d) {
                // var em = 10;
                var v = y(d.y) + em;
                var max = options.chart.plotHeight - em;
                return Math.round(v <= max ? v : v - em - 3);
            });

        labels.exit().remove();
    });

})();
