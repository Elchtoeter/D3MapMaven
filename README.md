# D3MapMaven

## Purpose and goals

This application is a demonstration of [graaljs's](https://github.com/oracle/graaljs) ability to load node.js modules and use them in a hosted JavaScript program.
Importantly the node modules don't have to bundled or adapted in any way.

## Modules used

To achieve the goal stated above, we implemented a data visualisation using [D3.js](https://d3js.org/) and [JSDOM](https://github.com/jsdom/jsdom) that is generated in a Java [Context](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/Context.html) and then hosted on a local [Helidon](https://helidon.io/) server running in the main Java application.
The reason for choosing those particular libraries are threefold. 
- They are both complex libraries that generate a large number of module loads via their dependencies. While we only ever directly load these two modules the module resolver algorithm in the GraalVM is called nearly 3000 times.
- D3.js is an ES6 module while JSDOM is a Common JS module allowing us to demonstrate the ability to laod both formats.
- D3.js is widely used and well documented so using it wouldn#t create any additional hurdles.

## Data used

The data visualisation that is being generated shows a map of Austrias political districts coloured according to the incidence of Covid-19 cases on a particular day. The subject matter was chosen because there was ample free data avialable and since the visualisation was not at the center of the project the choice was mostly arbitrary. [Covid-19 incidence data](https://www.data.gv.at/katalog/dataset/covid-19-daten-covid19-faelle-je-gkz/resource/91528b11-44cf-4c03-ad62-209f8a704f9b) was taken from the open data portal of the Austrian government and converted to JSON for convinence. Topographical data was taken from this [repository](https://github.com/ginseng666/GeoJSON-TopoJSON-Austria/blob/master/2021/simplified-99.5/bezirke_995_geo.json). Both sources are distributed under the Creative Commons license 4.0.

## Usage

### Changes to the GraalVM

As of May 2022 no changes to the GraalVM/graaljs are needed when building https://github.com/oracle/graaljs
When using that build the server can be started with  mvn compile exec:java
This will start the Netty web server used by Helidon. The service can then be reached by opening a browser and accessing
http://localhost:8000/ampel/ + year/month/day
where valid values for the date are between 2020/02/26 and 2021/12/08.
