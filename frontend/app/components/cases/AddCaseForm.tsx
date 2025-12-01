'use client';

import { useState, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { casesApi } from '../../../lib/api/cases';
import Input from '../common/Input';
import Button from '../common/Button';

export default function AddCaseForm() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    caseNumber: '',
    institution: '',
    customTitle: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [previewData, setPreviewData] = useState<any>(null);
  const [showPreview, setShowPreview] = useState(false);

  const validate = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.caseNumber.trim()) {
      newErrors.caseNumber = 'Case number is required';
    }

    if (!formData.institution.trim()) {
      newErrors.institution = 'Court institution is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handlePreview = async (e: FormEvent) => {
    e.preventDefault();
    
    if (!validate()) {
      return;
    }

    setLoading(true);
    try {
      const data = await casesApi.fetchCaseData(formData.caseNumber, formData.institution);
      setPreviewData(data);
      setShowPreview(true);
    } catch (error: any) {
      setErrors({ preview: error.message || 'Failed to fetch case data' });
      setShowPreview(false);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    
    if (!validate()) {
      return;
    }

    setLoading(true);
    try {
      const created = await casesApi.createCase({
        caseNumber: formData.caseNumber,
        institution: formData.institution,
        customTitle: formData.customTitle || undefined,
      });
      
      // Redirect to case details
      router.push(`/cases/${created.id}`);
    } catch (error: any) {
      setErrors({ submit: error.message || 'Failed to create case' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {errors.submit && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-sm text-red-800">{errors.submit}</p>
        </div>
      )}

      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Case Information</h2>
        
        <div className="space-y-4">
          <Input
            label="Case Number *"
            type="text"
            value={formData.caseNumber}
            onChange={(e) => setFormData({ ...formData, caseNumber: e.target.value })}
            placeholder="e.g., 12345/2025"
            error={errors.caseNumber}
            disabled={loading}
            required
          />

          <Input
            label="Court Institution *"
            type="text"
            value={formData.institution}
            onChange={(e) => setFormData({ ...formData, institution: e.target.value })}
            placeholder="e.g., TRIBUNALUL_BUCURESTI"
            error={errors.institution}
            helperText="Enter the court institution code"
            disabled={loading}
            required
          />

          <Input
            label="Custom Title (Optional)"
            type="text"
            value={formData.customTitle}
            onChange={(e) => setFormData({ ...formData, customTitle: e.target.value })}
            placeholder="e.g., Popescu vs Ionescu"
            helperText="A friendly name to help you identify this case"
            disabled={loading}
          />
        </div>
      </div>

      {/* Preview Section */}
      {showPreview && previewData && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Case Preview</h3>
          <div className="space-y-2 text-sm">
            <p><span className="font-medium">Case Number:</span> {previewData.number || formData.caseNumber}</p>
            <p><span className="font-medium">Institution:</span> {previewData.institution || formData.institution}</p>
            {previewData.subject && (
              <p><span className="font-medium">Subject:</span> {previewData.subject}</p>
            )}
            {previewData.proceduralStageName && (
              <p><span className="font-medium">Stage:</span> {previewData.proceduralStageName}</p>
            )}
            {previewData.parties && previewData.parties.length > 0 && (
              <p><span className="font-medium">Parties:</span> {previewData.parties.length}</p>
            )}
          </div>
        </div>
      )}

      {errors.preview && (
        <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
          <p className="text-sm text-yellow-800">{errors.preview}</p>
        </div>
      )}

      <div className="flex flex-col sm:flex-row gap-3 justify-end">
        <Button
          type="button"
          variant="outline"
          onClick={handlePreview}
          loading={loading}
          disabled={loading}
        >
          Preview Case
        </Button>
        <Button
          type="submit"
          variant="primary"
          loading={loading}
          disabled={loading}
        >
          Add Case
        </Button>
      </div>
    </form>
  );
}

