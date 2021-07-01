(ns qbits.auspex.protocols)

(defprotocol Then
  (-then [_ f] [_ f executor]))

(defprotocol FMap
  (-fmap [_ f] [_ f executor]))

(defprotocol Catch
  (-catch [_ f] [_ f pattern]))

(defprotocol Finally
  (-finally [_ f] [_ f pattern]))

(defprotocol Success!
  (-success! [_ val]))

(defprotocol Complete!
  (-complete! [_ f executor]))

(defprotocol Success?
  (-success? [_]))

(defprotocol Error!
  (-error! [_ val]))

(defprotocol Error?
  (-error? [_]))

(defprotocol Cancel!
  (-cancel! [_]))

(defprotocol Canceled?
  (-canceled? [_]))

(defprotocol Timeout!
  (-timeout! [_ timeout-ms] [_ timeout-ms timeout-val]))

(defprotocol Handle
  (-handle [_ f] [_ f executor]))

(defprotocol Realized?
  (-realized? [_]))

(defprotocol WhenComplete
  (-when-complete [_ f] [_ f executor]))

(defprotocol Empty
  (-empty [x]))

(defprotocol Wrap
  (-wrap [x]))

(defprotocol Future)
