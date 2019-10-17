(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', 'kotlin'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('kotlin'));
  else {
    if (typeof kotlin === 'undefined') {
      throw new Error("Error loading module 'napier'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'napier'.");
    }
    root.napier = factory(typeof napier === 'undefined' ? {} : napier, kotlin);
  }
}(this, function (_, Kotlin) {
  'use strict';
  var Kind_CLASS = Kotlin.Kind.CLASS;
  var Enum = Kotlin.kotlin.Enum;
  var throwISE = Kotlin.throwISE;
  var Kind_OBJECT = Kotlin.Kind.OBJECT;
  var ArrayList_init = Kotlin.kotlin.collections.ArrayList_init_287e2$;
  var Collection = Kotlin.kotlin.collections.Collection;
  var toString = Kotlin.toString;
  Napier$Level.prototype = Object.create(Enum.prototype);
  Napier$Level.prototype.constructor = Napier$Level;
  DebugAntilog.prototype = Object.create(Antilog.prototype);
  DebugAntilog.prototype.constructor = DebugAntilog;
  function Antilog() {
  }
  Antilog.prototype.isEnable_g70d64$ = function (priority, tag) {
    return true;
  };
  Antilog.prototype.log_bb4c75$ = function (priority, tag, throwable, message) {
    if (this.isEnable_g70d64$(priority, tag)) {
      this.performLog_bb4c75$(priority, tag, throwable, message);
    }
  };
  Antilog.prototype.rawLog_urq6nw$ = function (priority, tag, throwable, message) {
    this.performLog_bb4c75$(priority, tag, throwable, message);
  };
  Antilog.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Antilog',
    interfaces: []
  };
  function Napier() {
    Napier_instance = this;
    this.baseArray_0 = ArrayList_init();
  }
  function Napier$Level(name, ordinal) {
    Enum.call(this);
    this.name$ = name;
    this.ordinal$ = ordinal;
  }
  function Napier$Level_initFields() {
    Napier$Level_initFields = function () {
    };
    Napier$Level$VERBOSE_instance = new Napier$Level('VERBOSE', 0);
    Napier$Level$DEBUG_instance = new Napier$Level('DEBUG', 1);
    Napier$Level$INFO_instance = new Napier$Level('INFO', 2);
    Napier$Level$WARNING_instance = new Napier$Level('WARNING', 3);
    Napier$Level$ERROR_instance = new Napier$Level('ERROR', 4);
    Napier$Level$ASSERT_instance = new Napier$Level('ASSERT', 5);
  }
  var Napier$Level$VERBOSE_instance;
  function Napier$Level$VERBOSE_getInstance() {
    Napier$Level_initFields();
    return Napier$Level$VERBOSE_instance;
  }
  var Napier$Level$DEBUG_instance;
  function Napier$Level$DEBUG_getInstance() {
    Napier$Level_initFields();
    return Napier$Level$DEBUG_instance;
  }
  var Napier$Level$INFO_instance;
  function Napier$Level$INFO_getInstance() {
    Napier$Level_initFields();
    return Napier$Level$INFO_instance;
  }
  var Napier$Level$WARNING_instance;
  function Napier$Level$WARNING_getInstance() {
    Napier$Level_initFields();
    return Napier$Level$WARNING_instance;
  }
  var Napier$Level$ERROR_instance;
  function Napier$Level$ERROR_getInstance() {
    Napier$Level_initFields();
    return Napier$Level$ERROR_instance;
  }
  var Napier$Level$ASSERT_instance;
  function Napier$Level$ASSERT_getInstance() {
    Napier$Level_initFields();
    return Napier$Level$ASSERT_instance;
  }
  Napier$Level.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Level',
    interfaces: [Enum]
  };
  function Napier$Level$values() {
    return [Napier$Level$VERBOSE_getInstance(), Napier$Level$DEBUG_getInstance(), Napier$Level$INFO_getInstance(), Napier$Level$WARNING_getInstance(), Napier$Level$ERROR_getInstance(), Napier$Level$ASSERT_getInstance()];
  }
  Napier$Level.values = Napier$Level$values;
  function Napier$Level$valueOf(name) {
    switch (name) {
      case 'VERBOSE':
        return Napier$Level$VERBOSE_getInstance();
      case 'DEBUG':
        return Napier$Level$DEBUG_getInstance();
      case 'INFO':
        return Napier$Level$INFO_getInstance();
      case 'WARNING':
        return Napier$Level$WARNING_getInstance();
      case 'ERROR':
        return Napier$Level$ERROR_getInstance();
      case 'ASSERT':
        return Napier$Level$ASSERT_getInstance();
      default:throwISE('No enum constant com.github.aakira.napier.Napier.Level.' + name);
    }
  }
  Napier$Level.valueOf_61zpoe$ = Napier$Level$valueOf;
  Napier.prototype.base_5zfcz4$ = function (antilog) {
    this.baseArray_0.add_11rb$(antilog);
  };
  Napier.prototype.isEnable_g70d64$ = function (priority, tag) {
    var $receiver = this.baseArray_0;
    var any$result;
    any$break: do {
      var tmp$;
      if (Kotlin.isType($receiver, Collection) && $receiver.isEmpty()) {
        any$result = false;
        break any$break;
      }
      tmp$ = $receiver.iterator();
      while (tmp$.hasNext()) {
        var element = tmp$.next();
        if (element.isEnable_g70d64$(priority, tag)) {
          any$result = true;
          break any$break;
        }
      }
      any$result = false;
    }
     while (false);
    return any$result;
  };
  Napier.prototype.rawLog_bb4c75$ = function (priority, tag, throwable, message) {
    var tmp$;
    tmp$ = this.baseArray_0.iterator();
    while (tmp$.hasNext()) {
      var element = tmp$.next();
      element.rawLog_urq6nw$(priority, tag, throwable, message);
    }
  };
  Napier.prototype.v_82mfvz$ = function (message, throwable, tag) {
    if (throwable === void 0)
      throwable = null;
    if (tag === void 0)
      tag = null;
    this.log_na0abi$(Napier$Level$VERBOSE_getInstance(), tag, throwable, message);
  };
  Napier.prototype.i_82mfvz$ = function (message, throwable, tag) {
    if (throwable === void 0)
      throwable = null;
    if (tag === void 0)
      tag = null;
    this.log_na0abi$(Napier$Level$INFO_getInstance(), tag, throwable, message);
  };
  Napier.prototype.d_82mfvz$ = function (message, throwable, tag) {
    if (throwable === void 0)
      throwable = null;
    if (tag === void 0)
      tag = null;
    this.log_na0abi$(Napier$Level$DEBUG_getInstance(), tag, throwable, message);
  };
  Napier.prototype.w_82mfvz$ = function (message, throwable, tag) {
    if (throwable === void 0)
      throwable = null;
    if (tag === void 0)
      tag = null;
    this.log_na0abi$(Napier$Level$WARNING_getInstance(), tag, throwable, message);
  };
  Napier.prototype.e_82mfvz$ = function (message, throwable, tag) {
    if (throwable === void 0)
      throwable = null;
    if (tag === void 0)
      tag = null;
    this.log_na0abi$(Napier$Level$ERROR_getInstance(), tag, throwable, message);
  };
  Napier.prototype.wtf_82mfvz$ = function (message, throwable, tag) {
    if (throwable === void 0)
      throwable = null;
    if (tag === void 0)
      tag = null;
    this.log_na0abi$(Napier$Level$ASSERT_getInstance(), tag, throwable, message);
  };
  Napier.prototype.log_na0abi$ = function (priority, tag, throwable, message) {
    if (tag === void 0)
      tag = null;
    if (throwable === void 0)
      throwable = null;
    if (this.isEnable_g70d64$(priority, tag)) {
      this.rawLog_bb4c75$(priority, tag, throwable, message);
    }
  };
  Napier.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Napier',
    interfaces: []
  };
  var Napier_instance = null;
  function Napier_getInstance() {
    if (Napier_instance === null) {
      new Napier();
    }
    return Napier_instance;
  }
  function DebugAntilog(defaultTag) {
    Antilog.call(this);
    this.defaultTag_0 = defaultTag;
  }
  DebugAntilog.prototype.isEnable_g70d64$ = function (priority, tag) {
    return priority !== Napier$Level$VERBOSE_getInstance();
  };
  DebugAntilog.prototype.performLog_bb4c75$ = function (priority, tag, throwable, message) {
    var tmp$, tmp$_0;
    var logTag = tag != null ? tag : this.defaultTag_0;
    if (message != null) {
      if (throwable != null) {
        tmp$_0 = toString(message) + '\n' + toString(throwable.message);
      }
       else {
        tmp$_0 = message;
      }
    }
     else {
      tmp$ = throwable != null ? throwable.message : null;
      if (tmp$ == null) {
        return;
      }
      tmp$_0 = tmp$;
    }
    var fullMessage = tmp$_0;
    switch (priority.name) {
      case 'VERBOSE':
        console.log('VERBOSE ' + logTag + ' : ' + fullMessage);
        break;
      case 'DEBUG':
        console.log('DEBUG ' + logTag + ' : ' + fullMessage);
        break;
      case 'INFO':
        console.info('INFO ' + logTag + ' : ' + fullMessage);
        break;
      case 'WARNING':
        console.warn('WARNING ' + logTag + ' : ' + fullMessage);
        break;
      case 'ERROR':
        console.error('ERROR ' + logTag + ' : ' + fullMessage);
        break;
      case 'ASSERT':
        console.error('ASSERT ' + logTag + ' : ' + fullMessage);
        break;
    }
  };
  DebugAntilog.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'DebugAntilog',
    interfaces: [Antilog]
  };
  var package$com = _.com || (_.com = {});
  var package$github = package$com.github || (package$com.github = {});
  var package$aakira = package$github.aakira || (package$github.aakira = {});
  var package$napier = package$aakira.napier || (package$aakira.napier = {});
  package$napier.Antilog = Antilog;
  Object.defineProperty(Napier$Level, 'VERBOSE', {
    get: Napier$Level$VERBOSE_getInstance
  });
  Object.defineProperty(Napier$Level, 'DEBUG', {
    get: Napier$Level$DEBUG_getInstance
  });
  Object.defineProperty(Napier$Level, 'INFO', {
    get: Napier$Level$INFO_getInstance
  });
  Object.defineProperty(Napier$Level, 'WARNING', {
    get: Napier$Level$WARNING_getInstance
  });
  Object.defineProperty(Napier$Level, 'ERROR', {
    get: Napier$Level$ERROR_getInstance
  });
  Object.defineProperty(Napier$Level, 'ASSERT', {
    get: Napier$Level$ASSERT_getInstance
  });
  Napier.prototype.Level = Napier$Level;
  Object.defineProperty(package$napier, 'Napier', {
    get: Napier_getInstance
  });
  package$napier.DebugAntilog = DebugAntilog;
  Kotlin.defineModule('napier', _);
  return _;
}));
