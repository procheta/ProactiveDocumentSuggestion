cd /home/procheta/ProactiveDocumentSuggestion/ProactiveDocumentSuggestion

CP=.
for jarfile in `find lib -name "*.jar"`
do
        CP=$CP:$jarfile
done
cd /home/procheta/ProactiveDocumentSuggestion/ProactiveDocumentSuggestion/build/classes
#do
java -cp $CP BaselineRetriever/RetrieveQueries 

