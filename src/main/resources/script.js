const jsdom =  require("jsdom");
const { JSDOM } = jsdom;
const austria = require('./bezirke_995_geo.json');
const cases = require('./CovidFaelleBezirk.json');

module.exports.getMap = async function(date){
    const { document } = (new JSDOM('')).window;
    global.document = document;
    var d3 = await import("d3");

    function scale(district) {
    var incidence = district[0]["AnzahlFaelle"]/district[0]["AnzEinwohner"]*100_000;
    if (incidence < 5) {
      return "#55BB66";
    } else if (incidence < 25){
      return "#AABB22";
    }else if (incidence <= 50){
      return "yellow";
    }else if (incidence <= 100){
      return "orange";
    }else if (incidence < 100000){
      return "red";
    } else {
      return "#ffffff"
    }
}

var body = d3.select(document).select("body");

var filteredCases = cases.filter(item => item["Time"].split(" ")[0]===date);

var w = 800;
var h = 600;

var projection = d3.geoMercator()
.translate([0, 0])
.scale(1);;

var geoGenerator = d3.geoPath()
.projection(projection)
.pointRadius(2);    

  var b = geoGenerator.bounds( austria ),
   s = .95 / Math.max((b[1][0] - b[0][0]) / w, (b[1][1] - b[0][1]) / h),
   t = [(w - s * (b[1][0] + b[0][0])) / 2, (h - s * (b[1][1] + b[0][1])) / 2];

   projection
        .scale(s)
        .translate(t);

var svg = body.append("svg")
    .attr("width", w)
    .attr("height", h);

svg.selectAll("path")
      .data(austria.features)
      .enter()
      .append("path")
      .attr("d", geoGenerator)
      .style("fill",function(it){return scale(filteredCases.filter(row => row["GKZ"]==it.properties.iso))})
      .style("stroke","#000");

return body.node().innerHTML;
}

