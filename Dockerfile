FROM alpine:3.20
RUN mkdir custom-providers
WORKDIR custom-providers
ADD ./passwordless/build/libs/passwordless-0.0.1-SNAPSHOT-plain.jar /custom-providers/passwordless-0.0.1-SNAPSHOT-plain.jar
ADD ./TermsAndConditions/target/TermsAndConditions-1.0-SNAPSHOT.jar /custom-providers/TermsAndConditions-1.0-SNAPSHOT.jar
