define [
  'hchart'
], ($)->
  options =
    chart:
      type: 'pie'
      renderTo: 'chartContainer'
    title:
      text: 'MyTest Title'
    subtitle:
      text: 'My test subtitle'
    yAxis:
      title:
        text: 'Total percent market share'
    tooltip:
      valueSuffix: '%'
    plotOptions:
      pie:
        shadow: false
        center: ['50%', '50%']
    series: [
      {
      name: 'Total Translations'
      size: '60%'
      dataLabels:
        color: 'white'
        distance: -30
        formatter: ()->
          return @point.name if @y > 5
          null
      }
      {
      name: 'Distinct translations'
      size: '80%'
      innerSize: '60%'
      dataLabels:
        formatter: ()->
          return "<b>#{@point.name}: </b> #{@y}" if @y > 1
          return null
      }
    ]

  reportChart = null


  showChart=(title, json, chart = Highcharts.charts[0])->
    return if !json
    console?.log json

    options.title.text = title
    options.subtitle.text = "Translation number: #{json.translationNum}"

    options.series[0].data = []

    name = 'MSIE'
    options.series[0].data.push
      name: name
      y: 55.11

    name = 'Firefox'
    options.series[0].data.push
      name: name
      y: 21.63

    name = 'Chrome'
    options.series[0].data.push
      name: name
      y: 11.94

    name = 'Safari'
    options.series[0].data.push
      name: name
      y: 7.15

    name = 'Opera'
    options.series[0].data.push
      name: name
      y: 2.14



    options.series[0].data.push ['Duplicated translations', json.translationNum - json.distinctTranslationNum]


    options.series[1].data = [
      ['Translated', json.translatedNum]
      ['Untranslated', json.untranslatedNum]
    ]
    reportChart = new Highcharts.Chart options

  showChart: showChart
  reportChart: reportChart




