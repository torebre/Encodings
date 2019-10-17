if (typeof kotlin === 'undefined') {
  throw new Error("Error loading module 'webview-js'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'webview-js'.");
}
this['webview-js'] = function (_, Kotlin) {
  'use strict';
  var toString = Kotlin.toString;
  var throwCCE = Kotlin.throwCCE;
  var Kind_CLASS = Kotlin.Kind.CLASS;
  function KanjiApp() {
    var tmp$;
    this.canvas_0 = Kotlin.isType(tmp$ = document.getElementById('canvas'), HTMLCanvasElement) ? tmp$ : throwCCE();
  }
  KanjiApp.prototype.show = function () {
    var tmp$;
    this.canvas_0.height = 500;
    this.canvas_0.width = 500;
    var context = this.canvas_0.getContext('2d');
    console.log('Canvas: ' + this.canvas_0);
    console.log('Context: ' + toString(context));
    var kanjiViewer = new KanjiViewer(new Bounds(0, 0, 500, 500), Kotlin.isType(tmp$ = context, CanvasRenderingContext2D) ? tmp$ : throwCCE());
    kanjiViewer.drawKanji();
  };
  KanjiApp.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'KanjiApp',
    interfaces: []
  };
  function KanjiViewer(bounds, context) {
    this.bounds_0 = bounds;
    this.context_0 = context;
  }
  KanjiViewer.prototype.drawKanji = function () {
    console.log('Test23');
    this.context_0.lineWidth = 10.0;
    this.context_0.strokeRect(100.0, 100.0, 100.0, 100.0);
  };
  KanjiViewer.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'KanjiViewer',
    interfaces: []
  };
  function Bounds(xMin, yMin, xMax, yMax) {
    this.xMin = xMin;
    this.yMin = yMin;
    this.xMax = xMax;
    this.yMax = yMax;
  }
  Bounds.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Bounds',
    interfaces: []
  };
  Bounds.prototype.component1 = function () {
    return this.xMin;
  };
  Bounds.prototype.component2 = function () {
    return this.yMin;
  };
  Bounds.prototype.component3 = function () {
    return this.xMax;
  };
  Bounds.prototype.component4 = function () {
    return this.yMax;
  };
  Bounds.prototype.copy_tjonv8$ = function (xMin, yMin, xMax, yMax) {
    return new Bounds(xMin === void 0 ? this.xMin : xMin, yMin === void 0 ? this.yMin : yMin, xMax === void 0 ? this.xMax : xMax, yMax === void 0 ? this.yMax : yMax);
  };
  Bounds.prototype.toString = function () {
    return 'Bounds(xMin=' + Kotlin.toString(this.xMin) + (', yMin=' + Kotlin.toString(this.yMin)) + (', xMax=' + Kotlin.toString(this.xMax)) + (', yMax=' + Kotlin.toString(this.yMax)) + ')';
  };
  Bounds.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.xMin) | 0;
    result = result * 31 + Kotlin.hashCode(this.yMin) | 0;
    result = result * 31 + Kotlin.hashCode(this.xMax) | 0;
    result = result * 31 + Kotlin.hashCode(this.yMax) | 0;
    return result;
  };
  Bounds.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.xMin, other.xMin) && Kotlin.equals(this.yMin, other.yMin) && Kotlin.equals(this.xMax, other.xMax) && Kotlin.equals(this.yMax, other.yMax)))));
  };
  function main(args) {
    var kanjiApp = new KanjiApp();
    kanjiApp.show();
  }
  var package$com = _.com || (_.com = {});
  var package$kjipo = package$com.kjipo || (package$com.kjipo = {});
  var package$viewer = package$kjipo.viewer || (package$kjipo.viewer = {});
  package$viewer.KanjiApp = KanjiApp;
  _.KanjiViewer = KanjiViewer;
  _.Bounds = Bounds;
  package$viewer.main_kand9s$ = main;
  main([]);
  Kotlin.defineModule('webview-js', _);
  return _;
}(typeof this['webview-js'] === 'undefined' ? {} : this['webview-js'], kotlin);

//# sourceMappingURL=webview-js.js.map
