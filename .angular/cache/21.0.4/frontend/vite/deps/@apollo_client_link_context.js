import {
  ApolloLink
} from "./chunk-6KBCFBRM.js";
import "./chunk-4NVJHVNJ.js";
import {
  Observable
} from "./chunk-NLOMSAMV.js";
import {
  __objRest
} from "./chunk-OC4HWNDI.js";

// node_modules/@apollo/client/link/context/index.js
function setContext(setter) {
  return new SetContextLink((prevContext, operation) => setter(operation, prevContext));
}
var SetContextLink = class extends ApolloLink {
  constructor(setter) {
    super((operation, forward) => {
      const request = __objRest(operation, []);
      Object.defineProperty(request, "client", {
        enumerable: false,
        value: operation.client
      });
      return new Observable((observer) => {
        let closed = false;
        Promise.resolve(request).then((req) => setter(operation.getContext(), req)).then(operation.setContext).then(() => {
          if (!closed) {
            forward(operation).subscribe(observer);
          }
        }).catch(observer.error.bind(observer));
        return () => {
          closed = true;
        };
      });
    });
  }
};
export {
  SetContextLink,
  setContext
};
//# sourceMappingURL=@apollo_client_link_context.js.map
