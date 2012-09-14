
language='Japanese'
col=['T','N','I'].map ()-> {name: "#{language}.#{this}", index: "#{language}.#{this}", width: 20, editable: false, align: 'center'}
a=[{name: "Other", index: "other", width: 20, editable: false, align: 'center'}]
a=a.concat col
console.log a.length