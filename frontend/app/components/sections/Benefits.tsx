import BenefitItem from './BenefitItem';

const benefits = [
  {
    title: 'No More Manual Checking',
    description: 'Stop manually checking court websites. We do it for you automatically.',
  },
  {
    title: 'Never Miss Deadlines',
    description: 'Get notified immediately when hearings are scheduled or deadlines approach.',
  },
  {
    title: 'Track Multiple Cases',
    description: 'Monitor as many cases as you need from different courts in one dashboard.',
  },
  {
    title: 'Professional Grade',
    description: 'Built specifically for legal professionals who need reliable, accurate information.',
  },
];

const stats = [
  { value: '100%', label: 'Automated', bgColor: 'bg-blue-50' },
  { value: '24/7', label: 'Monitoring', bgColor: 'bg-green-50' },
  { value: 'Instant', label: 'Notifications', bgColor: 'bg-purple-50' },
  { value: 'Unlimited', label: 'Cases', bgColor: 'bg-indigo-50' },
];

export default function Benefits() {
  return (
    <section className="py-12 sm:py-16 md:py-20 bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 sm:gap-12 items-center">
          <div>
            <h2 className="text-2xl sm:text-3xl md:text-4xl font-bold text-gray-900 mb-6">
              Save Time, Reduce Stress
            </h2>
            <div className="space-y-4">
              {benefits.map((benefit, index) => (
                <BenefitItem
                  key={index}
                  title={benefit.title}
                  description={benefit.description}
                />
              ))}
            </div>
          </div>
          <div className="bg-white p-6 sm:p-8 rounded-lg shadow-lg">
            <div className="text-center mb-6">
              <div className="text-3xl sm:text-4xl font-bold text-blue-600 mb-2">100%</div>
              <div className="text-sm sm:text-base text-gray-600">Automated</div>
            </div>
            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              {stats.map((stat, index) => (
                <div key={index} className={`p-3 sm:p-4 ${stat.bgColor} rounded-lg text-center`}>
                  <div className="text-xl sm:text-2xl font-bold text-gray-900">{stat.value}</div>
                  <div className="text-xs sm:text-sm text-gray-600">{stat.label}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

