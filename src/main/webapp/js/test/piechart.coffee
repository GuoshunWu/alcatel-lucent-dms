$(($)->
  colors = Highcharts.getOptions().colors
  data = [
    { name: 'MSIE', y: 55.11, drilldown: [
      {name: '6.0', y: 10.85}
      {name: '7.0', y: 7.35}
      {name: '8.0', y: 33.06}
      {name: '9.0', y: 2.81}
    ]}
    { name: 'Firefox', y: 21.63, drilldown: [
      {name: '2.0', y: 0.20}
      {name: '3.0', y: 0.83}
      {name: '3.5', y: 1.58}
      {name: '3.6', y: 13.12}
      {name: '4.0', y: 5.43}
    ]}
    { name: 'Chrome', y: 11.94, drilldown: [
      {name: '5.0', y: 0.12}
      {name: '6.0', y: 0.19}
      {name: '7.0', y: 0.12}
      {name: '8.0', y: 0.36}
      {name: '9.0', y: 0.32}
      {name: '10.0', y: 9.91}
      {name: '11.0', y: 0.50}
      {name: '12.0', y: 0.22}
    ]}
    { name: 'Safari', y: 7.15, drilldown: [
      {name: '5.0', y: 4.55}
      {name: '4.0', y: 1.42}
      {name: 'Win 5.0', y: 0.23}
      {name: '4.1', y: 0.21}
      {name: 'Safari/Maxthon', y: 0.20}
      {name: '3.1', y: 0.19}
      {name: 'Win 4.1', y: 0.14}
    ]}
    { name: 'Opera', y: 2.14, drilldown: [
      {name: '9.x', y: 0.12}
      {name: '10.x', y: 0.37}
      {name: '11.x', y: 1.65}
    ]}
  ]

  browserData = []
  versionsData = []
  for entry, idx in data
    browserData.push name: entry.name, y: entry.y, color: colors[idx]
    for subEntry, subIdx in entry.drilldown
      color = Highcharts.Color(colors[idx]).brighten(0.2 - subIdx / entry.drilldown.length / 5).get()
      versionsData.push name: "#{entry.name} #{subEntry.name}", y: subEntry.y, color: color

  $('#container').highcharts(
    chart:
      type: 'pie'
    title:
      text: 'Browser market share, April, 2011'
    yAxis:
      title:
        text: 'Total percent market share'
    plotOptions:
      pie:
        shadow: false,
        center: ['50%', '50%']
    tooltip:
      valueSuffix: '%'
    series: [
      {
      name: 'Browser',
      data: browserData,
      size: '60%',
      dataLabels:
        formatter: ()->(return @point.name if @y > 5);null
        color: 'white',
        distance: -30
      }
      {
      name: 'Version',
      data: versionsData,
      size: '80%',
      innerSize: '60%',
      dataLabels:
        formatter: ()->(return "<b>#{@point.name}: </b> #{@y}%" if @y > 1); null
      }
    ]
  )
)


