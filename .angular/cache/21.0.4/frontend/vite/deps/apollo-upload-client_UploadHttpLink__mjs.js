import {
  defaultPrinter,
  fallbackHttpConfig,
  parseAndCheckHttpResponse,
  rewriteURIForGET,
  selectHttpOptionsAndBodyInternal,
  selectURI
} from "./chunk-WIMS3ZYR.js";
import {
  ApolloLink,
  filterOperationVariables
} from "./chunk-6KBCFBRM.js";
import "./chunk-4NVJHVNJ.js";
import {
  Observable
} from "./chunk-NLOMSAMV.js";
import "./chunk-OC4HWNDI.js";

// node_modules/is-plain-obj/index.js
function isPlainObject(value) {
  if (typeof value !== "object" || value === null) {
    return false;
  }
  const prototype = Object.getPrototypeOf(value);
  return (prototype === null || prototype === Object.prototype || Object.getPrototypeOf(prototype) === null) && !(Symbol.toStringTag in value) && !(Symbol.iterator in value);
}

// node_modules/extract-files/extractFiles.mjs
function extractFiles(value, isExtractable, path = "") {
  if (!arguments.length) throw new TypeError("Argument 1 `value` is required.");
  if (typeof isExtractable !== "function")
    throw new TypeError("Argument 2 `isExtractable` must be a function.");
  if (typeof path !== "string")
    throw new TypeError("Argument 3 `path` must be a string.");
  const clones = /* @__PURE__ */ new Map();
  const files = /* @__PURE__ */ new Map();
  function recurse(value2, path2, recursed) {
    if (isExtractable(value2)) {
      const filePaths = files.get(value2);
      filePaths ? filePaths.push(path2) : files.set(value2, [path2]);
      return null;
    }
    const valueIsList = Array.isArray(value2) || typeof FileList !== "undefined" && value2 instanceof FileList;
    const valueIsPlainObject = isPlainObject(value2);
    if (valueIsList || valueIsPlainObject) {
      let clone = clones.get(value2);
      const uncloned = !clone;
      if (uncloned) {
        clone = valueIsList ? [] : (
          // Replicate if the plain object is an `Object` instance.
          value2 instanceof /** @type {any} */
          Object ? {} : /* @__PURE__ */ Object.create(null)
        );
        clones.set(
          value2,
          /** @type {Clone} */
          clone
        );
      }
      if (!recursed.has(value2)) {
        const pathPrefix = path2 ? `${path2}.` : "";
        const recursedDeeper = new Set(recursed).add(value2);
        if (valueIsList) {
          let index = 0;
          for (const item of value2) {
            const itemClone = recurse(
              item,
              pathPrefix + index++,
              recursedDeeper
            );
            if (uncloned) clone.push(itemClone);
          }
        } else
          for (const key in value2) {
            const propertyClone = recurse(
              value2[key],
              pathPrefix + key,
              recursedDeeper
            );
            if (uncloned)
              clone[key] = propertyClone;
          }
      }
      return clone;
    }
    return value2;
  }
  return {
    clone: recurse(value, path, /* @__PURE__ */ new Set()),
    files
  };
}

// node_modules/apollo-upload-client/formDataAppendFile.mjs
function formDataAppendFile(formData, fieldName, file) {
  "name" in file ? formData.append(fieldName, file, file.name) : formData.append(fieldName, file);
}

// node_modules/extract-files/isExtractableFile.mjs
function isExtractableFile(value) {
  return typeof File !== "undefined" && value instanceof File || typeof Blob !== "undefined" && value instanceof Blob;
}

// node_modules/apollo-upload-client/UploadHttpLink.mjs
var UploadHttpLink = class extends ApolloLink {
  /**
   * @param {object} options Options.
   * @param {Parameters<typeof selectURI>[1]} [options.uri] GraphQL endpoint
   *   URI. Defaults to `"/graphql"`.
   * @param {boolean} [options.useGETForQueries] Should GET be used to fetch
   *   queries, if there are no files to upload.
   * @param {ExtractableFileMatcher} [options.isExtractableFile] Matches
   *   extractable files in the GraphQL operation. Defaults to
   *   {@linkcode isExtractableFile}.
   * @param {typeof FormData} [options.FormData]
   *   [`FormData`](https://developer.mozilla.org/en-US/docs/Web/API/FormData)
   *   class. Defaults to the {@linkcode FormData} global.
   * @param {FormDataFileAppender} [options.formDataAppendFile]
   *   Customizes how extracted files are appended to the
   *   [`FormData`](https://developer.mozilla.org/en-US/docs/Web/API/FormData)
   *   instance. Defaults to {@linkcode formDataAppendFile}.
   * @param {BaseHttpLink.Printer} [options.print] Prints the GraphQL query or
   *   mutation AST to a string for transport. Defaults to
   *   {@linkcode defaultPrinter}.
   * @param {typeof fetch} [options.fetch]
   *   [`fetch`](https://fetch.spec.whatwg.org) implementation. Defaults to the
   *   {@linkcode fetch} global.
   * @param {RequestInit} [options.fetchOptions] `fetch` options; overridden by
   *   upload requirements.
   * @param {string} [options.credentials] Overrides
   *   {@linkcode RequestInit.credentials credentials} in
   *   {@linkcode fetchOptions}.
   * @param {{ [headerName: string]: string }} [options.headers] Merges with and
   *   overrides {@linkcode RequestInit.headers headers} in
   *   {@linkcode fetchOptions}.
   * @param {boolean} [options.includeExtensions] Toggles sending `extensions`
   *   fields to the GraphQL server. Defaults to `false`.
   * @param {boolean} [options.includeUnusedVariables] Toggles including unused
   *   GraphQL variables in the request. Defaults to `false`.
   */
  constructor({
    uri: fetchUri = "/graphql",
    useGETForQueries,
    isExtractableFile: customIsExtractableFile = isExtractableFile,
    FormData: CustomFormData,
    formDataAppendFile: customFormDataAppendFile = formDataAppendFile,
    print = defaultPrinter,
    fetch: customFetch,
    fetchOptions,
    credentials,
    headers,
    includeExtensions,
    includeUnusedVariables = false
  } = {}) {
    super(
      (operation) => new Observable((observer) => {
        const context = operation.getContext();
        const { options, body } = selectHttpOptionsAndBodyInternal(
          operation,
          print,
          fallbackHttpConfig,
          {
            http: {
              includeExtensions
            },
            options: fetchOptions,
            credentials,
            headers
          },
          {
            http: context.http,
            options: context.fetchOptions,
            credentials: context.credentials,
            headers: context.headers
          }
        );
        if (body.variables && !includeUnusedVariables)
          body.variables = filterOperationVariables(
            body.variables,
            operation.query
          );
        const { clone, files } = extractFiles(
          body,
          customIsExtractableFile,
          ""
        );
        let uri = selectURI(operation, fetchUri);
        if (files.size) {
          if (options.headers)
            delete options.headers["content-type"];
          const RuntimeFormData = CustomFormData || FormData;
          const form = new RuntimeFormData();
          form.append("operations", JSON.stringify(clone));
          const map = {};
          let i = 0;
          files.forEach((paths) => {
            map[++i] = paths;
          });
          form.append("map", JSON.stringify(map));
          i = 0;
          files.forEach((_paths, file) => {
            customFormDataAppendFile(form, String(++i), file);
          });
          options.body = form;
        } else {
          if (useGETForQueries && // If the operation contains some mutations GET shouldn’t be used.
          !operation.query.definitions.some(
            (definition) => definition.kind === "OperationDefinition" && definition.operation === "mutation"
          ))
            options.method = "GET";
          if (options.method === "GET") {
            const result = (
              /** @type {{ newURI: string } | { parseError: unknown }} */
              // The return type is incorrect; `newURI` and `parseError`
              // will never both be present.
              rewriteURIForGET(uri, body)
            );
            if ("parseError" in result) throw result.parseError;
            uri = result.newURI;
          } else options.body = JSON.stringify(clone);
        }
        let controller;
        if (typeof AbortController !== "undefined") {
          controller = new AbortController();
          if (options.signal)
            options.signal.aborted ? (
              // Signal already aborted, so immediately abort.
              controller.abort()
            ) : (
              // Signal not already aborted, so setup a listener to abort
              // when it does.
              options.signal.addEventListener(
                "abort",
                () => {
                  controller.abort();
                },
                {
                  // Prevent a memory leak if the user configured abort
                  // controller is long lasting, or controls multiple
                  // things.
                  once: true
                }
              )
            );
          options.signal = controller.signal;
        }
        const runtimeFetch = customFetch || fetch;
        let cleaningUp;
        runtimeFetch(uri, options).then((response) => {
          operation.setContext({ response });
          return response;
        }).then(parseAndCheckHttpResponse(operation)).then((result) => {
          observer.next(result);
          observer.complete();
        }).catch((error) => {
          if (!cleaningUp) observer.error(error);
        });
        return () => {
          cleaningUp = true;
          if (controller) controller.abort();
        };
      })
    );
  }
};
export {
  UploadHttpLink as default
};
//# sourceMappingURL=apollo-upload-client_UploadHttpLink__mjs.js.map
