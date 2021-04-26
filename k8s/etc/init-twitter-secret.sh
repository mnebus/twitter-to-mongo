#!/usr/bin/env zsh

##################################################################
# Install the twitter secret with the twitter creds
# TODO - throw an error if the TWITTER_* variables aren't set
# ..or ask if twitter setup is required
##################################################################
twitterApiKey=`echo -n $TWITTER_API_KEY | base64`
twiterApiKeySecret=`echo -n $TWITTER_API_KEY_SECRET | base64`
twitterAccessCode=`echo -n $TWITTER_ACCESS_TOKEN | base64`
twitterAccessCodeSecret=`echo -n $TWITTER_ACCESS_TOKEN_SECRET | base64`

cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Secret
metadata:
  name: twitter-secret
type: Opaque
data:
  twitter.api.key: ${twitterApiKey}
  twitter.api.key.secret: ${twiterApiKeySecret}
  twitter.access.token: ${twitterAccessCode}
  twitter.access.token.secret: ${twitterAccessCodeSecret}
EOF