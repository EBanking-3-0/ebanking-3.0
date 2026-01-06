import {
  ApolloLink,
  AutoCleanedWeakCache,
  Kind,
  StrongCache,
  Trie,
  WeakCache,
  __DEV__,
  cacheSizes,
  checkDocument,
  compact,
  filterOperationVariables,
  getOperationDefinition,
  invariant,
  isNonNullObject,
  maybe,
  print,
  registerGlobalCache,
  visit
} from "./chunk-NC2TGXKB.js";
import {
  Observable
} from "./chunk-NLOMSAMV.js";
import {
  __async,
  __asyncGenerator,
  __await,
  __forAwait,
  __objRest,
  __spreadProps,
  __spreadValues
} from "./chunk-OC4HWNDI.js";

// node_modules/@wry/context/lib/slot.js
var currentContext = null;
var MISSING_VALUE = {};
var idCounter = 1;
var makeSlotClass = () => class Slot {
  constructor() {
    this.id = [
      "slot",
      idCounter++,
      Date.now(),
      Math.random().toString(36).slice(2)
    ].join(":");
  }
  hasValue() {
    for (let context = currentContext; context; context = context.parent) {
      if (this.id in context.slots) {
        const value = context.slots[this.id];
        if (value === MISSING_VALUE)
          break;
        if (context !== currentContext) {
          currentContext.slots[this.id] = value;
        }
        return true;
      }
    }
    if (currentContext) {
      currentContext.slots[this.id] = MISSING_VALUE;
    }
    return false;
  }
  getValue() {
    if (this.hasValue()) {
      return currentContext.slots[this.id];
    }
  }
  withValue(value, callback, args, thisArg) {
    const slots = {
      __proto__: null,
      [this.id]: value
    };
    const parent = currentContext;
    currentContext = { parent, slots };
    try {
      return callback.apply(thisArg, args);
    } finally {
      currentContext = parent;
    }
  }
  // Capture the current context and wrap a callback function so that it
  // reestablishes the captured context when called.
  static bind(callback) {
    const context = currentContext;
    return function() {
      const saved = currentContext;
      try {
        currentContext = context;
        return callback.apply(this, arguments);
      } finally {
        currentContext = saved;
      }
    };
  }
  // Immediately run a callback function without any captured context.
  static noContext(callback, args, thisArg) {
    if (currentContext) {
      const saved = currentContext;
      try {
        currentContext = null;
        return callback.apply(thisArg, args);
      } finally {
        currentContext = saved;
      }
    } else {
      return callback.apply(thisArg, args);
    }
  }
};
function maybe2(fn) {
  try {
    return fn();
  } catch (ignored) {
  }
}
var globalKey = "@wry/context:Slot";
var host = (
  // Prefer globalThis when available.
  // https://github.com/benjamn/wryware/issues/347
  maybe2(() => globalThis) || // Fall back to global, which works in Node.js and may be converted by some
  // bundlers to the appropriate identifier (window, self, ...) depending on the
  // bundling target. https://github.com/endojs/endo/issues/576#issuecomment-1178515224
  maybe2(() => global) || // Otherwise, use a dummy host that's local to this module. We used to fall
  // back to using the Array constructor as a namespace, but that was flagged in
  // https://github.com/benjamn/wryware/issues/347, and can be avoided.
  /* @__PURE__ */ Object.create(null)
);
var globalHost = host;
var Slot = globalHost[globalKey] || // Earlier versions of this package stored the globalKey property on the Array
// constructor, so we check there as well, to prevent Slot class duplication.
Array[globalKey] || (function(Slot2) {
  try {
    Object.defineProperty(globalHost, globalKey, {
      value: Slot2,
      enumerable: false,
      writable: false,
      // When it was possible for globalHost to be the Array constructor (a
      // legacy Slot dedup strategy), it was important for the property to be
      // configurable:true so it could be deleted. That does not seem to be as
      // important when globalHost is the global object, but I don't want to
      // cause similar problems again, and configurable:true seems safest.
      // https://github.com/endojs/endo/issues/576#issuecomment-1178274008
      configurable: true
    });
  } finally {
    return Slot2;
  }
})(makeSlotClass());

// node_modules/@wry/context/lib/index.js
var { bind, noContext } = Slot;

// node_modules/optimism/lib/context.js
var parentEntrySlot = new Slot();

// node_modules/optimism/lib/helpers.js
var { hasOwnProperty } = Object.prototype;
var arrayFromSet = Array.from || function(set) {
  const array = [];
  set.forEach((item) => array.push(item));
  return array;
};
function maybeUnsubscribe(entryOrDep) {
  const { unsubscribe } = entryOrDep;
  if (typeof unsubscribe === "function") {
    entryOrDep.unsubscribe = void 0;
    unsubscribe();
  }
}

// node_modules/optimism/lib/entry.js
var emptySetPool = [];
var POOL_TARGET_SIZE = 100;
function assert(condition, optionalMessage) {
  if (!condition) {
    throw new Error(optionalMessage || "assertion failure");
  }
}
function valueIs(a, b) {
  const len = a.length;
  return (
    // Unknown values are not equal to each other.
    len > 0 && // Both values must be ordinary (or both exceptional) to be equal.
    len === b.length && // The underlying value or exception must be the same.
    a[len - 1] === b[len - 1]
  );
}
function valueGet(value) {
  switch (value.length) {
    case 0:
      throw new Error("unknown value");
    case 1:
      return value[0];
    case 2:
      throw value[1];
  }
}
function valueCopy(value) {
  return value.slice(0);
}
var Entry = class _Entry {
  constructor(fn) {
    this.fn = fn;
    this.parents = /* @__PURE__ */ new Set();
    this.childValues = /* @__PURE__ */ new Map();
    this.dirtyChildren = null;
    this.dirty = true;
    this.recomputing = false;
    this.value = [];
    this.deps = null;
    ++_Entry.count;
  }
  peek() {
    if (this.value.length === 1 && !mightBeDirty(this)) {
      rememberParent(this);
      return this.value[0];
    }
  }
  // This is the most important method of the Entry API, because it
  // determines whether the cached this.value can be returned immediately,
  // or must be recomputed. The overall performance of the caching system
  // depends on the truth of the following observations: (1) this.dirty is
  // usually false, (2) this.dirtyChildren is usually null/empty, and thus
  // (3) valueGet(this.value) is usually returned without recomputation.
  recompute(args) {
    assert(!this.recomputing, "already recomputing");
    rememberParent(this);
    return mightBeDirty(this) ? reallyRecompute(this, args) : valueGet(this.value);
  }
  setDirty() {
    if (this.dirty)
      return;
    this.dirty = true;
    reportDirty(this);
    maybeUnsubscribe(this);
  }
  dispose() {
    this.setDirty();
    forgetChildren(this);
    eachParent(this, (parent, child) => {
      parent.setDirty();
      forgetChild(parent, this);
    });
  }
  forget() {
    this.dispose();
  }
  dependOn(dep2) {
    dep2.add(this);
    if (!this.deps) {
      this.deps = emptySetPool.pop() || /* @__PURE__ */ new Set();
    }
    this.deps.add(dep2);
  }
  forgetDeps() {
    if (this.deps) {
      arrayFromSet(this.deps).forEach((dep2) => dep2.delete(this));
      this.deps.clear();
      emptySetPool.push(this.deps);
      this.deps = null;
    }
  }
};
Entry.count = 0;
function rememberParent(child) {
  const parent = parentEntrySlot.getValue();
  if (parent) {
    child.parents.add(parent);
    if (!parent.childValues.has(child)) {
      parent.childValues.set(child, []);
    }
    if (mightBeDirty(child)) {
      reportDirtyChild(parent, child);
    } else {
      reportCleanChild(parent, child);
    }
    return parent;
  }
}
function reallyRecompute(entry, args) {
  forgetChildren(entry);
  parentEntrySlot.withValue(entry, recomputeNewValue, [entry, args]);
  if (maybeSubscribe(entry, args)) {
    setClean(entry);
  }
  return valueGet(entry.value);
}
function recomputeNewValue(entry, args) {
  entry.recomputing = true;
  const { normalizeResult } = entry;
  let oldValueCopy;
  if (normalizeResult && entry.value.length === 1) {
    oldValueCopy = valueCopy(entry.value);
  }
  entry.value.length = 0;
  try {
    entry.value[0] = entry.fn.apply(null, args);
    if (normalizeResult && oldValueCopy && !valueIs(oldValueCopy, entry.value)) {
      try {
        entry.value[0] = normalizeResult(entry.value[0], oldValueCopy[0]);
      } catch (_a) {
      }
    }
  } catch (e) {
    entry.value[1] = e;
  }
  entry.recomputing = false;
}
function mightBeDirty(entry) {
  return entry.dirty || !!(entry.dirtyChildren && entry.dirtyChildren.size);
}
function setClean(entry) {
  entry.dirty = false;
  if (mightBeDirty(entry)) {
    return;
  }
  reportClean(entry);
}
function reportDirty(child) {
  eachParent(child, reportDirtyChild);
}
function reportClean(child) {
  eachParent(child, reportCleanChild);
}
function eachParent(child, callback) {
  const parentCount = child.parents.size;
  if (parentCount) {
    const parents = arrayFromSet(child.parents);
    for (let i = 0; i < parentCount; ++i) {
      callback(parents[i], child);
    }
  }
}
function reportDirtyChild(parent, child) {
  assert(parent.childValues.has(child));
  assert(mightBeDirty(child));
  const parentWasClean = !mightBeDirty(parent);
  if (!parent.dirtyChildren) {
    parent.dirtyChildren = emptySetPool.pop() || /* @__PURE__ */ new Set();
  } else if (parent.dirtyChildren.has(child)) {
    return;
  }
  parent.dirtyChildren.add(child);
  if (parentWasClean) {
    reportDirty(parent);
  }
}
function reportCleanChild(parent, child) {
  assert(parent.childValues.has(child));
  assert(!mightBeDirty(child));
  const childValue = parent.childValues.get(child);
  if (childValue.length === 0) {
    parent.childValues.set(child, valueCopy(child.value));
  } else if (!valueIs(childValue, child.value)) {
    parent.setDirty();
  }
  removeDirtyChild(parent, child);
  if (mightBeDirty(parent)) {
    return;
  }
  reportClean(parent);
}
function removeDirtyChild(parent, child) {
  const dc = parent.dirtyChildren;
  if (dc) {
    dc.delete(child);
    if (dc.size === 0) {
      if (emptySetPool.length < POOL_TARGET_SIZE) {
        emptySetPool.push(dc);
      }
      parent.dirtyChildren = null;
    }
  }
}
function forgetChildren(parent) {
  if (parent.childValues.size > 0) {
    parent.childValues.forEach((_value, child) => {
      forgetChild(parent, child);
    });
  }
  parent.forgetDeps();
  assert(parent.dirtyChildren === null);
}
function forgetChild(parent, child) {
  child.parents.delete(parent);
  parent.childValues.delete(child);
  removeDirtyChild(parent, child);
}
function maybeSubscribe(entry, args) {
  if (typeof entry.subscribe === "function") {
    try {
      maybeUnsubscribe(entry);
      entry.unsubscribe = entry.subscribe.apply(null, args);
    } catch (e) {
      entry.setDirty();
      return false;
    }
  }
  return true;
}

// node_modules/optimism/lib/dep.js
var EntryMethods = {
  setDirty: true,
  dispose: true,
  forget: true
  // Fully remove parent Entry from LRU cache and computation graph
};
function dep(options) {
  const depsByKey = /* @__PURE__ */ new Map();
  const subscribe = options && options.subscribe;
  function depend(key) {
    const parent = parentEntrySlot.getValue();
    if (parent) {
      let dep2 = depsByKey.get(key);
      if (!dep2) {
        depsByKey.set(key, dep2 = /* @__PURE__ */ new Set());
      }
      parent.dependOn(dep2);
      if (typeof subscribe === "function") {
        maybeUnsubscribe(dep2);
        dep2.unsubscribe = subscribe(key);
      }
    }
  }
  depend.dirty = function dirty(key, entryMethodName) {
    const dep2 = depsByKey.get(key);
    if (dep2) {
      const m = entryMethodName && hasOwnProperty.call(EntryMethods, entryMethodName) ? entryMethodName : "setDirty";
      arrayFromSet(dep2).forEach((entry) => entry[m]());
      depsByKey.delete(key);
      maybeUnsubscribe(dep2);
    }
  };
  return depend;
}

// node_modules/optimism/lib/index.js
var defaultKeyTrie;
function defaultMakeCacheKey(...args) {
  const trie = defaultKeyTrie || (defaultKeyTrie = new Trie(typeof WeakMap === "function"));
  return trie.lookupArray(args);
}
var caches = /* @__PURE__ */ new Set();
function wrap(originalFunction, { max = Math.pow(2, 16), keyArgs, makeCacheKey = defaultMakeCacheKey, normalizeResult, subscribe, cache: cacheOption = StrongCache } = /* @__PURE__ */ Object.create(null)) {
  const cache = typeof cacheOption === "function" ? new cacheOption(max, (entry) => entry.dispose()) : cacheOption;
  const optimistic = function() {
    const key = makeCacheKey.apply(null, keyArgs ? keyArgs.apply(null, arguments) : arguments);
    if (key === void 0) {
      return originalFunction.apply(null, arguments);
    }
    let entry = cache.get(key);
    if (!entry) {
      cache.set(key, entry = new Entry(originalFunction));
      entry.normalizeResult = normalizeResult;
      entry.subscribe = subscribe;
      entry.forget = () => cache.delete(key);
    }
    const value = entry.recompute(Array.prototype.slice.call(arguments));
    cache.set(key, entry);
    caches.add(cache);
    if (!parentEntrySlot.hasValue()) {
      caches.forEach((cache2) => cache2.clean());
      caches.clear();
    }
    return value;
  };
  Object.defineProperty(optimistic, "size", {
    get: () => cache.size,
    configurable: false,
    enumerable: false
  });
  Object.freeze(optimistic.options = {
    max,
    keyArgs,
    makeCacheKey,
    normalizeResult,
    subscribe,
    cache
  });
  function dirtyKey(key) {
    const entry = key && cache.get(key);
    if (entry) {
      entry.setDirty();
    }
  }
  optimistic.dirtyKey = dirtyKey;
  optimistic.dirty = function dirty() {
    dirtyKey(makeCacheKey.apply(null, arguments));
  };
  function peekKey(key) {
    const entry = key && cache.get(key);
    if (entry) {
      return entry.peek();
    }
  }
  optimistic.peekKey = peekKey;
  optimistic.peek = function peek() {
    return peekKey(makeCacheKey.apply(null, arguments));
  };
  function forgetKey(key) {
    return key ? cache.delete(key) : false;
  }
  optimistic.forgetKey = forgetKey;
  optimistic.forget = function forget() {
    return forgetKey(makeCacheKey.apply(null, arguments));
  };
  optimistic.makeCacheKey = makeCacheKey;
  optimistic.getKey = keyArgs ? function getKey() {
    return makeCacheKey.apply(null, keyArgs.apply(null, arguments));
  } : makeCacheKey;
  return Object.freeze(optimistic);
}

// node_modules/@apollo/client/utilities/graphql/DocumentTransform.js
function identity(document) {
  return document;
}
var DocumentTransform = class _DocumentTransform {
  transform;
  cached;
  resultCache = /* @__PURE__ */ new WeakSet();
  // This default implementation of getCacheKey can be overridden by providing
  // options.getCacheKey to the DocumentTransform constructor. In general, a
  // getCacheKey function may either return an array of keys (often including
  // the document) to be used as a cache key, or undefined to indicate the
  // transform for this document should not be cached.
  getCacheKey(document) {
    return [document];
  }
  /**
   * Creates a DocumentTransform that returns the input document unchanged.
   *
   * @returns The input document
   */
  static identity() {
    return new _DocumentTransform(identity, { cache: false });
  }
  /**
   * Creates a DocumentTransform that conditionally applies one of two transforms.
   *
   * @param predicate - Function that determines which transform to apply
   * @param left - Transform to apply when `predicate` returns `true`
   * @param right - Transform to apply when `predicate` returns `false`. If not provided, it defaults to `DocumentTransform.identity()`.
   * @returns A DocumentTransform that conditionally applies a document transform based on the predicate
   *
   * @example
   *
   * ```ts
   * import { isQueryOperation } from "@apollo/client/utilities";
   *
   * const conditionalTransform = DocumentTransform.split(
   *   (document) => isQueryOperation(document),
   *   queryTransform,
   *   mutationTransform
   * );
   * ```
   */
  static split(predicate, left, right = _DocumentTransform.identity()) {
    return Object.assign(new _DocumentTransform(
      (document) => {
        const documentTransform = predicate(document) ? left : right;
        return documentTransform.transformDocument(document);
      },
      // Reasonably assume both `left` and `right` transforms handle their own caching
      { cache: false }
    ), { left, right });
  }
  constructor(transform, options = {}) {
    this.transform = transform;
    if (options.getCacheKey) {
      this.getCacheKey = options.getCacheKey;
    }
    this.cached = options.cache !== false;
    this.resetCache();
  }
  /**
   * Resets the internal cache of this transform, if it is cached.
   */
  resetCache() {
    if (this.cached) {
      const stableCacheKeys = new Trie();
      this.performWork = wrap(_DocumentTransform.prototype.performWork.bind(this), {
        makeCacheKey: (document) => {
          const cacheKeys = this.getCacheKey(document);
          if (cacheKeys) {
            invariant(Array.isArray(cacheKeys), 20);
            return stableCacheKeys.lookupArray(cacheKeys);
          }
        },
        max: cacheSizes["documentTransform.cache"],
        cache: WeakCache
      });
    }
  }
  performWork(document) {
    checkDocument(document);
    return this.transform(document);
  }
  /**
   * Transforms a GraphQL document using the configured transform function.
   *
   * @remarks
   *
   * Note that `transformDocument` caches the transformed document. Calling
   * `transformDocument` again with the already-transformed document will
   * immediately return it.
   *
   * @param document - The GraphQL document to transform
   * @returns The transformed document
   *
   * @example
   *
   * ```ts
   * const document = gql`
   *   # ...
   * `;
   *
   * const documentTransform = new DocumentTransform(transformFn);
   * const transformedDocument = documentTransform.transformDocument(document);
   * ```
   */
  transformDocument(document) {
    if (this.resultCache.has(document)) {
      return document;
    }
    const transformedDocument = this.performWork(document);
    this.resultCache.add(transformedDocument);
    return transformedDocument;
  }
  /**
   * Combines this document transform with another document transform. The
   * returned document transform first applies the current document transform,
   * then applies the other document transform.
   *
   * @param otherTransform - The transform to apply after this one
   * @returns A new DocumentTransform that applies both transforms in sequence
   *
   * @example
   *
   * ```ts
   * const combinedTransform = addTypenameTransform.concat(
   *   removeDirectivesTransform
   * );
   * ```
   */
  concat(otherTransform) {
    return Object.assign(new _DocumentTransform(
      (document) => {
        return otherTransform.transformDocument(this.transformDocument(document));
      },
      // Reasonably assume both transforms handle their own caching
      { cache: false }
    ), {
      left: this,
      right: otherTransform
    });
  }
  /**
  * @internal
  * Used to iterate through all transforms that are concatenations or `split` links.
  * 
  * @deprecated This is an internal API and should not be used directly. This can be removed or changed at any time.
  */
  left;
  /**
  * @internal
  * Used to iterate through all transforms that are concatenations or `split` links.
  * 
  * @deprecated This is an internal API and should not be used directly. This can be removed or changed at any time.
  */
  right;
};

// node_modules/@apollo/client/utilities/graphql/storeUtils.js
function isReference(obj) {
  return Boolean(obj && typeof obj === "object" && typeof obj.__ref === "string");
}

// node_modules/@apollo/client/utilities/isNetworkRequestSettled.js
function isNetworkRequestSettled(networkStatus) {
  return networkStatus === 7 || networkStatus === 8;
}

// node_modules/@apollo/client/utilities/graphql/print.js
var printCache;
var print2 = Object.assign((ast) => {
  let result = printCache.get(ast);
  if (!result) {
    result = print(ast);
    printCache.set(ast, result);
  }
  return result;
}, {
  reset() {
    printCache = new AutoCleanedWeakCache(
      cacheSizes.print || 2e3
      /* defaultCacheSizes.print */
    );
  }
});
print2.reset();
if (__DEV__) {
  registerGlobalCache("print", () => printCache ? printCache.size : 0);
}

// node_modules/@apollo/client/utilities/graphql/transform.js
var TYPENAME_FIELD = {
  kind: Kind.FIELD,
  name: {
    kind: Kind.NAME,
    value: "__typename"
  }
};
var addTypenameToDocument = Object.assign(function(doc) {
  return visit(doc, {
    SelectionSet: {
      enter(node, _key, parent) {
        if (parent && parent.kind === Kind.OPERATION_DEFINITION) {
          return;
        }
        const { selections } = node;
        if (!selections) {
          return;
        }
        const skip = selections.some((selection) => {
          return selection.kind === Kind.FIELD && (selection.name.value === "__typename" || selection.name.value.lastIndexOf("__", 0) === 0);
        });
        if (skip) {
          return;
        }
        const field = parent;
        if (field.kind === Kind.FIELD && field.directives && field.directives.some((d) => d.name.value === "export")) {
          return;
        }
        return __spreadProps(__spreadValues({}, node), {
          selections: [...selections, TYPENAME_FIELD]
        });
      }
    }
  });
}, {
  added(field) {
    return field === TYPENAME_FIELD;
  }
});

// node_modules/@apollo/client/utilities/graphql/operations.js
function isOperation(document, operation) {
  return getOperationDefinition(document)?.operation === operation;
}
function isMutationOperation(document) {
  return isOperation(document, "mutation");
}
function isSubscriptionOperation(document) {
  return isOperation(document, "subscription");
}

// node_modules/@apollo/client/utilities/isNetworkRequestInFlight.js
function isNetworkRequestInFlight(networkStatus) {
  return !isNetworkRequestSettled(networkStatus);
}

// node_modules/@apollo/client/errors/utils.js
function isBranded(error, name) {
  return typeof error === "object" && error !== null && error[Symbol.for("apollo.error")] === name;
}
function brand(error) {
  Object.defineProperty(error, Symbol.for("apollo.error"), {
    value: error.name,
    enumerable: false,
    writable: false,
    configurable: false
  });
}

// node_modules/@apollo/client/errors/CombinedProtocolErrors.js
function defaultFormatMessage(errors) {
  return errors.map((e) => e.message || "Error message not found.").join("\n");
}
var CombinedProtocolErrors = class _CombinedProtocolErrors extends Error {
  /**
   * A method that determines whether an error is a `CombinedProtocolErrors`
   * object. This method enables TypeScript to narrow the error type.
   *
   * @example
   *
   * ```ts
   * if (CombinedProtocolErrors.is(error)) {
   *   // TypeScript now knows `error` is a CombinedProtocolErrors object
   *   console.log(error.errors);
   * }
   * ```
   */
  static is(error) {
    return isBranded(error, "CombinedProtocolErrors");
  }
  /**
  * A function that formats the error message used for the error's `message`
  * property. Override this method to provide your own formatting.
  * 
  * @remarks
  * 
  * The `formatMessage` function is called by the `CombinedProtocolErrors`
  * constructor to provide a formatted message as the `message` property of the
  * `CombinedProtocolErrors` object. Follow the ["Providing a custom message
  * formatter"](https://www.apollographql.com/docs/react/api/errors/CombinedProtocolErrors#providing-a-custom-message-formatter) guide to learn how to modify the message format.
  * 
  * @param errors - The array of GraphQL errors returned from the server in the
  * `errors` field of the response.
  * @param options - Additional context that could be useful when formatting
  * the message.
  */
  static formatMessage = defaultFormatMessage;
  /**
  * The raw list of errors returned by the top-level `errors` field in the
  * multipart HTTP subscription response.
  */
  errors;
  constructor(protocolErrors) {
    super(_CombinedProtocolErrors.formatMessage(protocolErrors, {
      defaultFormatMessage
    }));
    this.name = "CombinedProtocolErrors";
    this.errors = protocolErrors;
    brand(this);
    Object.setPrototypeOf(this, _CombinedProtocolErrors.prototype);
  }
};

// node_modules/@apollo/client/errors/UnconventionalError.js
var UnconventionalError = class _UnconventionalError extends Error {
  /**
   * A method that determines whether an error is an `UnconventionalError`
   * object. This method enables TypeScript to narrow the error type.
   *
   * @example
   *
   * ```ts
   * if (UnconventionalError.is(error)) {
   *   // TypeScript now knows `error` is a UnconventionalError object
   *   console.log("What caused this?", error.cause);
   * }
   * ```
   */
  static is(error) {
    return isBranded(error, "UnconventionalError");
  }
  constructor(errorType) {
    super("An error of unexpected shape occurred.", { cause: errorType });
    this.name = "UnconventionalError";
    brand(this);
    Object.setPrototypeOf(this, _UnconventionalError.prototype);
  }
};

// node_modules/@apollo/client/errors/CombinedGraphQLErrors.js
function defaultFormatMessage2(errors) {
  return errors.filter((e) => e).map((e) => e.message || "Error message not found.").join("\n");
}
var CombinedGraphQLErrors = class _CombinedGraphQLErrors extends Error {
  /**
  * A method that determines whether an error is a `CombinedGraphQLErrors`
  * object. This method enables TypeScript to narrow the error type.
  * 
  * @example
  * 
  * ```ts
  * if (CombinedGraphQLErrors.is(error)) {
  *   // TypeScript now knows `error` is a `CombinedGraphQLErrors` object
  *   console.log(error.errors);
  * }
  * ```
  */
  static is(error) {
    return isBranded(error, "CombinedGraphQLErrors");
  }
  /**
  * A function that formats the error message used for the error's `message`
  * property. Override this method to provide your own formatting.
  * 
  * @remarks
  * 
  * The `formatMessage` function is called by the `CombinedGraphQLErrors`
  * constructor to provide a formatted message as the `message` property of the
  * `CombinedGraphQLErrors` object. Follow the ["Providing a custom message
  * formatter"](https://www.apollographql.com/docs/react/api/errors/CombinedGraphQLErrors#providing-a-custom-message-formatter) guide to learn how to modify the message format.
  * 
  * @param errors - The array of GraphQL errors returned from the server in
  * the `errors` field of the response.
  * @param options - Additional context that could be useful when formatting
  * the message.
  */
  static formatMessage = defaultFormatMessage2;
  /**
  * The raw list of GraphQL errors returned by the `errors` field in the GraphQL response.
  */
  errors;
  /**
  * Partial data returned in the `data` field of the GraphQL response.
  */
  data;
  /**
  * Extensions returned by the `extensions` field in the GraphQL response.
  */
  extensions;
  constructor(result, errors = result.errors || []) {
    super(_CombinedGraphQLErrors.formatMessage(errors, {
      result,
      defaultFormatMessage: defaultFormatMessage2
    }));
    this.errors = errors;
    this.data = result.data;
    this.extensions = result.extensions;
    this.name = "CombinedGraphQLErrors";
    brand(this);
    Object.setPrototypeOf(this, _CombinedGraphQLErrors.prototype);
  }
};

// node_modules/@apollo/client/errors/LinkError.js
var registry = /* @__PURE__ */ new WeakSet();
function registerLinkError(error) {
  registry.add(error);
}
var LinkError = {
  /**
   * A method that determines whether an error originated from the link chain.
   * `is` does not provide any type narrowing.
   *
   * @example
   *
   * ```ts
   * if (LinkError.is(error)) {
   *   // The error originated from the link chain
   *   console.log("Got network error:", error.message);
   * }
   * ```
   */
  is: (error) => registry.has(error)
};

// node_modules/@apollo/client/errors/LocalStateError.js
var LocalStateError = class _LocalStateError extends Error {
  /**
   * A method that determines whether an error is a `LocalStateError`
   * object. This method enables TypeScript to narrow the error type.
   *
   * @example
   *
   * ```ts
   * if (LocalStateError.is(error)) {
   *   // TypeScript now knows `error` is a LocalStateError object
   *   console.log(error.path);
   * }
   * ```
   */
  static is(error) {
    return isBranded(error, "LocalStateError");
  }
  /**
  * The path to the field that caused the error.
  */
  path;
  constructor(message, options = {}) {
    super(message, { cause: options.sourceError });
    this.name = "LocalStateError";
    this.path = options.path;
    brand(this);
    Object.setPrototypeOf(this, _LocalStateError.prototype);
  }
};

// node_modules/@apollo/client/errors/ServerError.js
var ServerError = class _ServerError extends Error {
  /**
   * A method that determines whether an error is a `ServerError` object. This
   * method enables TypeScript to narrow the error type.
   *
   * @example
   *
   * ```ts
   * if (ServerError.is(error)) {
   *   // TypeScript now knows `error` is a ServerError object
   *   console.log(error.errors);
   * }
   * ```
   */
  static is(error) {
    return isBranded(error, "ServerError");
  }
  /**
  * The raw [`Response`](https://developer.mozilla.org/en-US/docs/Web/API/Response) object provided by the [Fetch API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API).
  */
  response;
  /**
  * The status code returned by the server in the response. This is provided as
  * a shortcut for `response.status`.
  */
  statusCode;
  /**
  * The raw response body text.
  */
  bodyText;
  constructor(message, options) {
    super(message);
    this.name = "ServerError";
    this.response = options.response;
    this.statusCode = options.response.status;
    this.bodyText = options.bodyText;
    brand(this);
    Object.setPrototypeOf(this, _ServerError.prototype);
  }
};

// node_modules/@apollo/client/errors/ServerParseError.js
var ServerParseError = class _ServerParseError extends Error {
  /**
   * A method that determines whether an error is a `ServerParseError`
   * object. This method enables TypeScript to narrow the error type.
   *
   * @example
   *
   * ```ts
   * if (ServerParseError.is(error)) {
   *   // TypeScript now knows `error` is a ServerParseError object
   *   console.log(error.statusCode);
   * }
   * ```
   */
  static is(error) {
    return isBranded(error, "ServerParseError");
  }
  /**
  * The raw [`Response`](https://developer.mozilla.org/en-US/docs/Web/API/Response) object provided by the [Fetch API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API).
  */
  response;
  /**
  * The status code returned by the server in the response. This is provided
  * as a shortcut for `response.status`.
  */
  statusCode;
  /**
  * The raw response body text.
  */
  bodyText;
  constructor(originalParseError, options) {
    super(originalParseError instanceof Error ? originalParseError.message : "Could not parse server response", { cause: originalParseError });
    this.name = "ServerParseError";
    this.response = options.response;
    this.statusCode = options.response.status;
    this.bodyText = options.bodyText;
    brand(this);
    Object.setPrototypeOf(this, _ServerParseError.prototype);
  }
};

// node_modules/@apollo/client/errors/isErrorLike.js
function isErrorLike(error) {
  return error !== null && typeof error === "object" && typeof error.message === "string" && typeof error.name === "string" && (typeof error.stack === "string" || typeof error.stack === "undefined");
}

// node_modules/@apollo/client/errors/index.js
var PROTOCOL_ERRORS_SYMBOL = Symbol();
function graphQLResultHasProtocolErrors(result) {
  if ("extensions" in result) {
    return CombinedProtocolErrors.is(result.extensions[PROTOCOL_ERRORS_SYMBOL]);
  }
  return false;
}
function toErrorLike(error) {
  if (isErrorLike(error)) {
    return error;
  }
  if (typeof error === "string") {
    return new Error(error, { cause: error });
  }
  return new UnconventionalError(error);
}

// node_modules/@apollo/client/link/http/parseAndCheckHttpResponse.js
var { hasOwnProperty: hasOwnProperty2 } = Object.prototype;
function isApolloPayloadResult(value) {
  return isNonNullObject(value) && "payload" in value;
}
function consumeMultipartBody(response) {
  return __asyncGenerator(this, null, function* () {
    const decoder = new TextDecoder("utf-8");
    const contentType = response.headers?.get("content-type");
    const match = contentType?.match(
      /*
        ;\s*boundary=                # Match the boundary parameter
        (?:                          # either
          '([^']*)'                  # a string starting with ' doesn't contain ', ends with '
          |                          # or
          "([^"]*)"                  # a string starting with " doesn't contain ", ends with "
          |                          # or
          ([^"'].*?)                 # a string that doesn't start with ' or ", parsed non-greedily
          )                          # end of the group
        \s*                          # optional whitespace
        (?:;|$)                        # match a semicolon or end of string
      */
      /;\s*boundary=(?:'([^']+)'|"([^"]+)"|([^"'].+?))\s*(?:;|$)/i
    );
    const boundary = "\r\n--" + (match ? match[1] ?? match[2] ?? match[3] ?? "-" : "-");
    let buffer = "";
    invariant(response.body && typeof response.body.getReader === "function", 60);
    const stream = response.body;
    const reader = stream.getReader();
    let done = false;
    let encounteredBoundary = false;
    let value;
    const passedFinalBoundary = () => encounteredBoundary && buffer[0] == "-" && buffer[1] == "-";
    try {
      while (!done) {
        ({ value, done } = yield new __await(reader.read()));
        const chunk = typeof value === "string" ? value : decoder.decode(value);
        const searchFrom = buffer.length - boundary.length + 1;
        buffer += chunk;
        let bi = buffer.indexOf(boundary, searchFrom);
        while (bi > -1 && !passedFinalBoundary()) {
          encounteredBoundary = true;
          let message;
          [message, buffer] = [
            buffer.slice(0, bi),
            buffer.slice(bi + boundary.length)
          ];
          const i = message.indexOf("\r\n\r\n");
          const headers = parseHeaders(message.slice(0, i));
          const contentType2 = headers["content-type"];
          if (contentType2 && contentType2.toLowerCase().indexOf("application/json") === -1) {
            throw new Error("Unsupported patch content type: application/json is required.");
          }
          const body = message.slice(i);
          if (body) {
            yield body;
          }
          bi = buffer.indexOf(boundary);
        }
        if (passedFinalBoundary()) {
          return;
        }
      }
      throw new Error("premature end of multipart body");
    } finally {
      reader.cancel();
    }
  });
}
function readMultipartBody(response, nextValue) {
  return __async(this, null, function* () {
    try {
      for (var iter = __forAwait(consumeMultipartBody(response)), more, temp, error; more = !(temp = yield iter.next()).done; more = false) {
        const body = temp.value;
        const result = parseJsonEncoding(response, body);
        if (Object.keys(result).length == 0)
          continue;
        if (isApolloPayloadResult(result)) {
          if (Object.keys(result).length === 1 && result.payload === null) {
            return;
          }
          let next = __spreadValues({}, result.payload);
          if ("errors" in result) {
            next.extensions = __spreadProps(__spreadValues({}, next.extensions), {
              [PROTOCOL_ERRORS_SYMBOL]: new CombinedProtocolErrors(result.errors ?? [])
            });
          }
          nextValue(next);
        } else {
          nextValue(result);
        }
      }
    } catch (temp) {
      error = [temp];
    } finally {
      try {
        more && (temp = iter.return) && (yield temp.call(iter));
      } finally {
        if (error)
          throw error[0];
      }
    }
  });
}
function parseHeaders(headerText) {
  const headersInit = {};
  headerText.split("\n").forEach((line) => {
    const i = line.indexOf(":");
    if (i > -1) {
      const name = line.slice(0, i).trim().toLowerCase();
      const value = line.slice(i + 1).trim();
      headersInit[name] = value;
    }
  });
  return headersInit;
}
function parseJsonEncoding(response, bodyText) {
  if (response.status >= 300) {
    throw new ServerError(`Response not successful: Received status code ${response.status}`, { response, bodyText });
  }
  try {
    return JSON.parse(bodyText);
  } catch (err) {
    throw new ServerParseError(err, { response, bodyText });
  }
}
function parseGraphQLResponseJsonEncoding(response, bodyText) {
  try {
    return JSON.parse(bodyText);
  } catch (err) {
    throw new ServerParseError(err, { response, bodyText });
  }
}
function parseResponse(response, bodyText) {
  const contentType = response.headers.get("content-type");
  if (contentType?.includes("application/graphql-response+json")) {
    return parseGraphQLResponseJsonEncoding(response, bodyText);
  }
  return parseJsonEncoding(response, bodyText);
}
function parseAndCheckHttpResponse(operations) {
  return (response) => response.text().then((bodyText) => {
    const result = parseResponse(response, bodyText);
    if (!Array.isArray(result) && !hasOwnProperty2.call(result, "data") && !hasOwnProperty2.call(result, "errors")) {
      throw new ServerError(`Server response was malformed for query '${Array.isArray(operations) ? operations.map((op) => op.operationName) : operations.operationName}'.`, { response, bodyText });
    }
    return result;
  });
}

// node_modules/@apollo/client/link/http/selectHttpOptionsAndBody.js
var defaultHttpOptions = {
  includeQuery: true,
  includeExtensions: true,
  preserveHeaderCase: false
};
var defaultHeaders = {
  // headers are case insensitive (https://stackoverflow.com/a/5259004)
  accept: "application/graphql-response+json,application/json;q=0.9",
  // The content-type header describes the type of the body of the request, and
  // so it typically only is sent with requests that actually have bodies. One
  // could imagine that Apollo Client would remove this header when constructing
  // a GET request (which has no body), but we historically have not done that.
  // This means that browsers will preflight all Apollo Client requests (even
  // GET requests). Apollo Server's CSRF prevention feature (introduced in
  // AS3.7) takes advantage of this fact and does not block requests with this
  // header. If you want to drop this header from GET requests, then you should
  // probably replace it with a `apollo-require-preflight` header, or servers
  // with CSRF prevention enabled might block your GET request. See
  // https://www.apollographql.com/docs/apollo-server/security/cors/#preventing-cross-site-request-forgery-csrf
  // for more details.
  "content-type": "application/json"
};
var defaultOptions = {
  method: "POST"
};
var fallbackHttpConfig = {
  http: defaultHttpOptions,
  headers: defaultHeaders,
  options: defaultOptions
};
var defaultPrinter = (ast, printer) => printer(ast);
function selectHttpOptionsAndBody(operation, fallbackConfig, ...configs) {
  configs.unshift(fallbackConfig);
  return selectHttpOptionsAndBodyInternal(operation, defaultPrinter, ...configs);
}
function selectHttpOptionsAndBodyInternal(operation, printer, ...configs) {
  let options = {};
  let http = {};
  configs.forEach((config) => {
    options = __spreadProps(__spreadValues(__spreadValues({}, options), config.options), {
      headers: __spreadValues(__spreadValues({}, options.headers), config.headers)
    });
    if (config.credentials) {
      options.credentials = config.credentials;
    }
    options.headers.accept = (config.http?.accept || []).concat(options.headers.accept).join(",");
    http = __spreadValues(__spreadValues({}, http), config.http);
  });
  options.headers = removeDuplicateHeaders(options.headers, http.preserveHeaderCase);
  const { operationName, extensions, variables, query } = operation;
  const body = { operationName, variables };
  if (http.includeExtensions && Object.keys(extensions || {}).length)
    body.extensions = extensions;
  if (http.includeQuery)
    body.query = printer(query, print2);
  return {
    options,
    body
  };
}
function removeDuplicateHeaders(headers, preserveHeaderCase) {
  if (!preserveHeaderCase) {
    const normalizedHeaders2 = {};
    Object.keys(Object(headers)).forEach((name) => {
      normalizedHeaders2[name.toLowerCase()] = headers[name];
    });
    return normalizedHeaders2;
  }
  const headerData = {};
  Object.keys(Object(headers)).forEach((name) => {
    headerData[name.toLowerCase()] = {
      originalName: name,
      value: headers[name]
    };
  });
  const normalizedHeaders = {};
  Object.keys(headerData).forEach((name) => {
    normalizedHeaders[headerData[name].originalName] = headerData[name].value;
  });
  return normalizedHeaders;
}

// node_modules/@apollo/client/link/http/checkFetcher.js
var checkFetcher = (fetcher) => {
  invariant(fetcher || typeof fetch !== "undefined", 59);
};

// node_modules/@apollo/client/link/http/createSignalIfSupported.js
var createSignalIfSupported = () => {
  if (typeof AbortController === "undefined")
    return { controller: false, signal: false };
  const controller = new AbortController();
  const signal = controller.signal;
  return { controller, signal };
};

// node_modules/@apollo/client/link/http/selectURI.js
var selectURI = (operation, fallbackURI) => {
  const context = operation.getContext();
  const contextURI = context.uri;
  if (contextURI) {
    return contextURI;
  } else if (typeof fallbackURI === "function") {
    return fallbackURI(operation);
  } else {
    return fallbackURI || "/graphql";
  }
};

// node_modules/@apollo/client/link/http/rewriteURIForGET.js
function rewriteURIForGET(chosenURI, body) {
  const queryParams = [];
  const addQueryParam = (key, value) => {
    queryParams.push(`${key}=${encodeURIComponent(value)}`);
  };
  if ("query" in body) {
    addQueryParam("query", body.query);
  }
  if (body.operationName) {
    addQueryParam("operationName", body.operationName);
  }
  if (body.variables) {
    let serializedVariables;
    try {
      serializedVariables = JSON.stringify(body.variables);
    } catch (parseError) {
      return { parseError };
    }
    addQueryParam("variables", serializedVariables);
  }
  if (body.extensions) {
    let serializedExtensions;
    try {
      serializedExtensions = JSON.stringify(body.extensions);
    } catch (parseError) {
      return { parseError };
    }
    addQueryParam("extensions", serializedExtensions);
  }
  let fragment = "", preFragment = chosenURI;
  const fragmentStart = chosenURI.indexOf("#");
  if (fragmentStart !== -1) {
    fragment = chosenURI.substr(fragmentStart);
    preFragment = chosenURI.substr(0, fragmentStart);
  }
  const queryParamsPrefix = preFragment.indexOf("?") === -1 ? "?" : "&";
  const newURI = preFragment + queryParamsPrefix + queryParams.join("&") + fragment;
  return { newURI };
}

// node_modules/@apollo/client/link/client-awareness/ClientAwarenessLink.js
var ClientAwarenessLink = class extends ApolloLink {
  constructor(options = {}) {
    super((operation, forward) => {
      const client = operation.client;
      const clientOptions = client["queryManager"].clientOptions;
      const context = operation.getContext();
      {
        const { name, version, transport = "headers" } = compact({}, clientOptions.clientAwareness, options.clientAwareness, context.clientAwareness);
        if (transport === "headers") {
          operation.setContext(({ headers, extensions }) => {
            return {
              headers: compact(
                // setting these first so that they can be overridden by user-provided headers
                {
                  "apollographql-client-name": name,
                  "apollographql-client-version": version
                },
                headers
              )
            };
          });
        }
      }
      {
        const { transport = "extensions" } = compact({}, clientOptions.enhancedClientAwareness, options.enhancedClientAwareness);
        if (transport === "extensions") {
          operation.extensions = compact(
            // setting these first so that it can be overridden by user-provided extensions
            {
              clientLibrary: {
                name: "@apollo/client",
                version: client.version
              }
            },
            operation.extensions
          );
        }
      }
      return forward(operation);
    });
  }
};

// node_modules/@apollo/client/link/http/BaseHttpLink.js
var backupFetch = maybe(() => fetch);
function noop() {
}
var BaseHttpLink = class extends ApolloLink {
  constructor(options = {}) {
    let _a = options, {
      uri = "/graphql",
      fetch: preferredFetch,
      print: print3 = defaultPrinter,
      includeExtensions,
      preserveHeaderCase,
      useGETForQueries,
      includeUnusedVariables = false
    } = _a, requestOptions = __objRest(_a, [
      "uri",
      // use default global fetch if nothing passed in
      "fetch",
      "print",
      "includeExtensions",
      "preserveHeaderCase",
      "useGETForQueries",
      "includeUnusedVariables"
    ]);
    if (__DEV__) {
      checkFetcher(preferredFetch || backupFetch);
    }
    const linkConfig = {
      http: compact({ includeExtensions, preserveHeaderCase }),
      options: requestOptions.fetchOptions,
      credentials: requestOptions.credentials,
      headers: requestOptions.headers
    };
    super((operation) => {
      let chosenURI = selectURI(operation, uri);
      const context = operation.getContext();
      const http = __spreadValues({}, context.http);
      if (isSubscriptionOperation(operation.query)) {
        http.accept = [
          "multipart/mixed;boundary=graphql;subscriptionSpec=1.0",
          ...http.accept || []
        ];
      }
      const contextConfig = {
        http,
        options: context.fetchOptions,
        credentials: context.credentials,
        headers: context.headers
      };
      const { options: options2, body } = selectHttpOptionsAndBodyInternal(operation, print3, fallbackHttpConfig, linkConfig, contextConfig);
      if (body.variables && !includeUnusedVariables) {
        body.variables = filterOperationVariables(body.variables, operation.query);
      }
      let controller = new AbortController();
      let cleanupController = () => {
        controller = void 0;
      };
      if (options2.signal) {
        const externalSignal = options2.signal;
        const listener = () => {
          controller?.abort(externalSignal.reason);
        };
        externalSignal.addEventListener("abort", listener, { once: true });
        cleanupController = () => {
          controller?.signal.removeEventListener("abort", cleanupController);
          controller = void 0;
          externalSignal.removeEventListener("abort", listener);
          cleanupController = noop;
        };
        controller.signal.addEventListener("abort", cleanupController, {
          once: true
        });
      }
      options2.signal = controller.signal;
      if (useGETForQueries && !isMutationOperation(operation.query)) {
        options2.method = "GET";
      }
      return new Observable((observer) => {
        if (options2.method === "GET") {
          const { newURI, parseError } = rewriteURIForGET(chosenURI, body);
          if (parseError) {
            throw parseError;
          }
          chosenURI = newURI;
        } else {
          options2.body = JSON.stringify(body);
        }
        const currentFetch = preferredFetch || maybe(() => fetch) || backupFetch;
        const observerNext = observer.next.bind(observer);
        currentFetch(chosenURI, options2).then((response) => {
          operation.setContext({ response });
          const ctype = response.headers?.get("content-type");
          if (ctype !== null && /^multipart\/mixed/i.test(ctype)) {
            return readMultipartBody(response, observerNext);
          } else {
            return parseAndCheckHttpResponse(operation)(response).then(observerNext);
          }
        }).then(() => {
          cleanupController();
          observer.complete();
        }).catch((err) => {
          cleanupController();
          observer.error(err);
        });
        return () => {
          if (controller)
            controller.abort();
        };
      });
    });
  }
};

// node_modules/@apollo/client/link/http/HttpLink.js
var HttpLink = class extends ApolloLink {
  constructor(options = {}) {
    const { left, right, request } = ApolloLink.from([
      new ClientAwarenessLink(options),
      new BaseHttpLink(options)
    ]);
    super(request);
    Object.assign(this, { left, right });
  }
};
var createHttpLink = (options = {}) => new HttpLink(options);

export {
  Slot,
  dep,
  wrap,
  DocumentTransform,
  print2 as print,
  isReference,
  addTypenameToDocument,
  isNetworkRequestSettled,
  isNetworkRequestInFlight,
  CombinedProtocolErrors,
  UnconventionalError,
  CombinedGraphQLErrors,
  registerLinkError,
  LinkError,
  LocalStateError,
  ServerError,
  ServerParseError,
  PROTOCOL_ERRORS_SYMBOL,
  graphQLResultHasProtocolErrors,
  toErrorLike,
  parseAndCheckHttpResponse,
  fallbackHttpConfig,
  defaultPrinter,
  selectHttpOptionsAndBody,
  selectHttpOptionsAndBodyInternal,
  checkFetcher,
  createSignalIfSupported,
  selectURI,
  rewriteURIForGET,
  HttpLink,
  createHttpLink
};
//# sourceMappingURL=chunk-ISCHYNEM.js.map
